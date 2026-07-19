from uuid import UUID

from fastapi import APIRouter, Depends, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.api.deps import get_current_user
from app.db.session import get_db_session
from app.models.user import User
from app.schemas.medication import (
    ConfirmExtractedMedicationsRequest,
    MedicationCreate,
    MedicationResponse,
    MedicationUpdate,
)
from app.services.medication_service import (
    create_medication,
    create_medications_from_confirmed_extraction,
    delete_medication,
    get_medication,
    list_medications,
    update_medication,
)
from app.services.simplification_service import simplify_medication

router = APIRouter(prefix="/medications")


@router.post("/confirm-extracted", response_model=list[MedicationResponse])
async def confirm_extracted_medications(
    payload: ConfirmExtractedMedicationsRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session),
) -> list[MedicationResponse]:
    medications = await create_medications_from_confirmed_extraction(
        db,
        current_user.id,
        payload.job_id,
    )
    return [MedicationResponse.model_validate(item) for item in medications]


@router.get("", response_model=list[MedicationResponse])
async def get_medications(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session),
) -> list[MedicationResponse]:
    return await list_medications(db, current_user.id)


@router.post("", response_model=MedicationResponse, status_code=status.HTTP_201_CREATED)
async def post_medication(
    payload: MedicationCreate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session),
) -> MedicationResponse:
    return await create_medication(db, current_user.id, payload)


@router.get("/{medication_id}", response_model=MedicationResponse)
async def get_medication_detail(
    medication_id: UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session),
) -> MedicationResponse:
    return await get_medication(db, current_user.id, medication_id)


@router.post("/{medication_id}/simplify", response_model=MedicationResponse)
async def post_medication_simplification(
    medication_id: UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session),
) -> MedicationResponse:
    return await simplify_medication(db, current_user.id, medication_id)


@router.patch("/{medication_id}", response_model=MedicationResponse)
async def patch_medication(
    medication_id: UUID,
    payload: MedicationUpdate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session),
) -> MedicationResponse:
    return await update_medication(db, current_user.id, medication_id, payload)


@router.delete("/{medication_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_medication_endpoint(
    medication_id: UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session),
) -> None:
    await delete_medication(db, current_user.id, medication_id)
