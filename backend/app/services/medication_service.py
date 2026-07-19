from datetime import datetime, time
from uuid import UUID

from fastapi import HTTPException, status
from pydantic import ValidationError
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import selectinload

from app.models.medication import Medication
from app.models.medication_schedule import MedicationSchedule
from app.models.processing_job import ProcessingJob
from app.schemas.extraction import PrescriptionExtraction
from app.schemas.medication import MedicationCreate, MedicationUpdate


def _select_with_schedules():
    return select(Medication).options(selectinload(Medication.schedules))


async def list_medications(db: AsyncSession, user_id: UUID) -> list[Medication]:
    stmt = (
        _select_with_schedules()
        .where(Medication.user_id == user_id)
        .order_by(Medication.created_at.desc())
    )
    result = await db.execute(stmt)
    return list(result.scalars().unique().all())


async def get_medication(db: AsyncSession, user_id: UUID, medication_id: UUID) -> Medication:
    stmt = _select_with_schedules().where(
        Medication.id == medication_id,
        Medication.user_id == user_id,
    )
    result = await db.execute(stmt)
    medication = result.scalars().unique().one_or_none()

    if medication is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Medication not found.",
        )

    return medication


async def create_medication(
    db: AsyncSession,
    user_id: UUID,
    payload: MedicationCreate,
) -> Medication:
    medication = Medication(
        user_id=user_id,
        name=payload.name.strip(),
        strength=payload.strength,
        dosage_instruction=payload.dosage_instruction.strip(),
        purpose_simplified=payload.purpose_simplified,
        start_date=payload.start_date,
        end_date=payload.end_date,
    )
    medication.schedules = [
        MedicationSchedule(
            user_id=user_id,
            time_of_day=schedule.time_of_day,
            frequency_type=schedule.frequency_type,
            days_of_week=schedule.days_of_week,
            meal_relation=schedule.meal_relation,
            dose_amount=schedule.dose_amount,
            notes=schedule.notes,
        )
        for schedule in payload.schedules
    ]

    db.add(medication)
    await db.commit()

    return await get_medication(db, user_id, medication.id)


async def update_medication(
    db: AsyncSession,
    user_id: UUID,
    medication_id: UUID,
    payload: MedicationUpdate,
) -> Medication:
    medication = await get_medication(db, user_id, medication_id)

    updates = payload.model_dump(exclude_unset=True)
    if {"name", "strength", "dosage_instruction"} & updates.keys():
        medication.simplified_instruction = None
        medication.purpose_simplified = None
    for field, value in updates.items():
        setattr(medication, field, value)

    await db.commit()

    return await get_medication(db, user_id, medication_id)


async def delete_medication(db: AsyncSession, user_id: UUID, medication_id: UUID) -> None:
    medication = await get_medication(db, user_id, medication_id)
    await db.delete(medication)
    await db.commit()


async def create_medications_from_confirmed_extraction(
    db: AsyncSession,
    user_id: UUID,
    job_id: UUID,
) -> list[Medication]:
    existing_result = await db.execute(
        _select_with_schedules().where(
            Medication.user_id == user_id,
            Medication.source_job_id == job_id,
        )
    )
    existing = list(existing_result.scalars().unique().all())
    if existing:
        return existing

    job_result = await db.execute(
        select(ProcessingJob).where(
            ProcessingJob.id == job_id,
            ProcessingJob.user_id == user_id,
        )
    )
    job = job_result.scalar_one_or_none()
    if job is None:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Processing job not found.")
    if job.status != "completed" or job.confirmed_result_json is None:
        raise HTTPException(status.HTTP_409_CONFLICT, "The prescription has not been confirmed.")
    try:
        extraction = PrescriptionExtraction.model_validate(job.confirmed_result_json)
    except ValidationError as error:
        raise HTTPException(
            status.HTTP_422_UNPROCESSABLE_ENTITY,
            "The confirmed document is not a prescription.",
        ) from error

    selected = [item for item in extraction.medications if item.selected]
    if not selected:
        raise HTTPException(status.HTTP_422_UNPROCESSABLE_ENTITY, "No medicines were selected.")

    medications = []
    for item in selected:
        instruction_parts = [part for part in (item.dosage, item.frequency) if part]
        instruction = ". ".join(instruction_parts) or "Follow the confirmed prescription."
        medication = Medication(
            user_id=user_id,
            source_document_id=job.document_id,
            source_job_id=job.id,
            name=item.name.strip(),
            strength=item.strength,
            dosage_instruction=instruction,
            simplified_instruction=instruction,
            confidence_score=item.confidence,
            requires_review=False,
            status="active",
        )
        medication.schedules = [
            MedicationSchedule(
                user_id=user_id,
                time_of_day=parsed_time,
                frequency_type="daily",
                meal_relation=_meal_relation(item.meal_relation),
                dose_amount=item.dosage,
                notes="Created from a confirmed prescription.",
            )
            for value in item.times
            if (parsed_time := _parse_time(value)) is not None
        ]
        medications.append(medication)
        db.add(medication)

    await db.commit()
    result = await db.execute(
        _select_with_schedules().where(
            Medication.user_id == user_id,
            Medication.source_job_id == job_id,
        )
    )
    return list(result.scalars().unique().all())


def _parse_time(value: str) -> time | None:
    normalized = value.strip().upper()
    for pattern in ("%H:%M", "%H:%M:%S", "%I:%M %p", "%I %p"):
        try:
            return datetime.strptime(normalized, pattern).time()
        except ValueError:
            continue
    return None


def _meal_relation(value: str | None) -> str:
    allowed = {"before_food", "after_food", "with_food", "no_relation", "unknown"}
    return value if value in allowed else "unknown"
