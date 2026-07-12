from uuid import UUID

from fastapi import HTTPException, status
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import selectinload

from app.models.medication import Medication
from app.models.medication_schedule import MedicationSchedule
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
    for field, value in updates.items():
        setattr(medication, field, value)

    await db.commit()

    return await get_medication(db, user_id, medication_id)


async def delete_medication(db: AsyncSession, user_id: UUID, medication_id: UUID) -> None:
    medication = await get_medication(db, user_id, medication_id)
    await db.delete(medication)
    await db.commit()
