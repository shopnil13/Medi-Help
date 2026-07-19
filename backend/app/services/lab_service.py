import re
import uuid
from datetime import UTC, datetime
from uuid import UUID

from fastapi import HTTPException, status
from pydantic import ValidationError
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.biomarker import Biomarker
from app.models.processing_job import ProcessingJob
from app.models.vital_record import VitalRecord
from app.schemas.extraction import LabReportExtraction

NORMALIZED_NAMES = {
    "hba1c": ("hba1c", "HbA1c", "custom"),
    "hemoglobina1c": ("hba1c", "HbA1c", "custom"),
    "a1c": ("hba1c", "HbA1c", "custom"),
    "fastingglucose": ("fasting_glucose", "Fasting glucose", "blood_glucose"),
    "fastingbloodglucose": ("fasting_glucose", "Fasting glucose", "blood_glucose"),
    "fastingplasmaglucose": ("fasting_glucose", "Fasting glucose", "blood_glucose"),
    "glucose": ("blood_glucose", "Blood glucose", "blood_glucose"),
    "ldl": ("ldl", "LDL", "custom"),
    "ldlcholesterol": ("ldl", "LDL", "custom"),
    "lowdensitylipoprotein": ("ldl", "LDL", "custom"),
    "hdl": ("hdl", "HDL", "custom"),
    "hdlcholesterol": ("hdl", "HDL", "custom"),
    "highdensitylipoprotein": ("hdl", "HDL", "custom"),
    "triglyceride": ("triglycerides", "Triglycerides", "custom"),
    "triglycerides": ("triglycerides", "Triglycerides", "custom"),
    "hemoglobin": ("hemoglobin", "Hemoglobin", "custom"),
    "haemoglobin": ("hemoglobin", "Hemoglobin", "custom"),
    "hgb": ("hemoglobin", "Hemoglobin", "custom"),
    "creatinine": ("creatinine", "Creatinine", "custom"),
    "serumcreatinine": ("creatinine", "Creatinine", "custom"),
}


async def create_records_from_confirmed_lab(
    db: AsyncSession,
    user_id: UUID,
    job_id: UUID,
) -> tuple[list[Biomarker], list[VitalRecord]]:
    existing_biomarkers = await _list_biomarkers(db, user_id, job_id)
    if existing_biomarkers:
        return existing_biomarkers, await _list_vitals(db, user_id, job_id)

    result = await db.execute(
        select(ProcessingJob).where(
            ProcessingJob.id == job_id,
            ProcessingJob.user_id == user_id,
        )
    )
    job = result.scalar_one_or_none()
    if job is None:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Processing job not found.")
    if job.status != "completed" or job.confirmed_result_json is None:
        raise HTTPException(status.HTTP_409_CONFLICT, "The lab report has not been confirmed.")
    try:
        extraction = LabReportExtraction.model_validate(job.confirmed_result_json)
    except ValidationError as error:
        raise HTTPException(
            status.HTTP_422_UNPROCESSABLE_CONTENT,
            "The confirmed document is not a lab report.",
        ) from error

    selected = [item for item in extraction.biomarkers if item.selected]
    if not selected:
        raise HTTPException(
            status.HTTP_422_UNPROCESSABLE_CONTENT,
            "No biomarkers were selected.",
        )

    recorded_at = job.confirmed_at or job.completed_at or datetime.now(UTC)
    biomarkers: list[Biomarker] = []
    vital_records: list[VitalRecord] = []
    for item in selected:
        normalized_name, display_name, metric_type = normalize_biomarker_name(item.name)
        numeric_value = parse_numeric_value(item.value)
        biomarker = Biomarker(
            id=uuid.uuid4(),
            user_id=user_id,
            source_document_id=job.document_id,
            source_job_id=job.id,
            name=item.name.strip(),
            normalized_name=normalized_name,
            value_numeric=numeric_value,
            value_text=item.value.strip(),
            unit=item.unit.strip() if item.unit else None,
            reference_range_text=item.reference_range,
            status=reference_status(numeric_value, item.reference_range),
            recorded_at=recorded_at,
            confidence_score=item.confidence,
        )
        biomarkers.append(biomarker)
        db.add(biomarker)

        if numeric_value is not None and item.unit:
            note = (
                f"Reference range: {item.reference_range}"
                if item.reference_range
                else "Imported from a confirmed lab report."
            )
            vital = VitalRecord(
                user_id=user_id,
                metric_type=metric_type,
                metric_name=display_name,
                value_numeric=numeric_value,
                unit=item.unit.strip(),
                recorded_at=recorded_at,
                source="lab_report",
                source_document_id=job.document_id,
                source_job_id=job.id,
                source_biomarker_id=biomarker.id,
                notes=note,
            )
            vital_records.append(vital)
            db.add(vital)

    await db.commit()
    return await _list_biomarkers(db, user_id, job_id), await _list_vitals(db, user_id, job_id)


async def _list_biomarkers(
    db: AsyncSession,
    user_id: UUID,
    job_id: UUID,
) -> list[Biomarker]:
    result = await db.execute(
        select(Biomarker)
        .where(Biomarker.user_id == user_id, Biomarker.source_job_id == job_id)
        .order_by(Biomarker.created_at.asc(), Biomarker.id.asc())
    )
    return list(result.scalars().all())


async def _list_vitals(
    db: AsyncSession,
    user_id: UUID,
    job_id: UUID,
) -> list[VitalRecord]:
    result = await db.execute(
        select(VitalRecord)
        .where(VitalRecord.user_id == user_id, VitalRecord.source_job_id == job_id)
        .order_by(VitalRecord.created_at.asc(), VitalRecord.id.asc())
    )
    return list(result.scalars().all())


def normalize_biomarker_name(name: str) -> tuple[str, str, str]:
    cleaned = re.sub(r"[^a-z0-9]", "", name.lower())
    known = NORMALIZED_NAMES.get(cleaned)
    if known is not None:
        return known
    normalized = re.sub(r"[^a-z0-9]+", "_", name.lower()).strip("_")
    return normalized or "unknown", name.strip(), "custom"


def parse_numeric_value(value: str) -> float | None:
    match = re.search(r"[-+]?\d[\d,]*(?:\.\d+)?", value)
    if match is None:
        return None
    try:
        return float(match.group(0).replace(",", ""))
    except ValueError:
        return None


def reference_status(value: float | None, reference_range: str | None) -> str:
    if value is None or not reference_range:
        return "unknown"
    numbers = [
        float(item.replace(",", "")) for item in re.findall(r"\d[\d,]*(?:\.\d+)?", reference_range)
    ]
    if len(numbers) >= 2 and ("-" in reference_range or " to " in reference_range.lower()):
        low, high = numbers[0], numbers[1]
        if value < low:
            return "low"
        if value > high:
            return "high"
        return "normal"
    if numbers:
        threshold = numbers[0]
        if "<" in reference_range:
            return "normal" if value < threshold else "high"
        if ">" in reference_range:
            return "normal" if value > threshold else "low"
    return "unknown"
