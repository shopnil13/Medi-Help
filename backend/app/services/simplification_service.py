from uuid import UUID

from fastapi import HTTPException, status
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.biomarker import Biomarker
from app.processing.simplification import SimplificationProvider, get_simplification_provider
from app.processing.simplification_safety import (
    validate_biomarker_simplification,
    validate_medicine_simplification,
)
from app.services.medication_service import get_medication

LOW_CONFIDENCE_THRESHOLD = 0.75


async def simplify_medication(
    db: AsyncSession,
    user_id: UUID,
    medication_id: UUID,
    provider: SimplificationProvider | None = None,
):
    medication = await get_medication(db, user_id, medication_id)
    if medication.purpose_simplified and medication.simplified_instruction:
        return medication

    generated = await (provider or get_simplification_provider()).simplify_medicine(
        medication.name,
        medication.strength,
        medication.dosage_instruction,
    )
    safe, changed = validate_medicine_simplification(generated, medication.dosage_instruction)
    medication.purpose_simplified = safe.purpose
    medication.simplified_instruction = safe.how_to_take
    medication.requires_review = medication.requires_review or changed
    await db.commit()
    return await get_medication(db, user_id, medication_id)


async def get_biomarker(db: AsyncSession, user_id: UUID, biomarker_id: UUID) -> Biomarker:
    result = await db.execute(
        select(Biomarker).where(Biomarker.id == biomarker_id, Biomarker.user_id == user_id)
    )
    biomarker = result.scalar_one_or_none()
    if biomarker is None:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Biomarker not found.")
    return biomarker


async def simplify_biomarker(
    db: AsyncSession,
    user_id: UUID,
    biomarker_id: UUID,
    provider: SimplificationProvider | None = None,
) -> Biomarker:
    biomarker = await get_biomarker(db, user_id, biomarker_id)
    if (
        biomarker.explanation_simplified
        and biomarker.status_explanation
        and biomarker.details_simplified
    ):
        return biomarker

    generated = await (provider or get_simplification_provider()).simplify_biomarker(
        biomarker.name,
        biomarker.normalized_name,
        biomarker.status,
        biomarker.reference_range_text,
    )
    safe, changed = validate_biomarker_simplification(generated)
    biomarker.explanation_simplified = safe.explanation
    biomarker.status_explanation = safe.status_meaning
    biomarker.details_simplified = safe.more_details
    biomarker.ask_doctor = (
        changed
        or biomarker.status != "normal"
        or biomarker.confidence_score is None
        or float(biomarker.confidence_score) < LOW_CONFIDENCE_THRESHOLD
    )
    await db.commit()
    await db.refresh(biomarker)
    return biomarker
