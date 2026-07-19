from datetime import datetime

from fastapi import APIRouter, Depends, Query, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.api.deps import get_current_user
from app.db.session import get_db_session
from app.models.user import User
from app.schemas.vital import (
    ConfirmExtractedLabRequest,
    ConfirmExtractedLabResponse,
    VitalBulkCreate,
    VitalCreate,
    VitalMetricType,
    VitalResponse,
    VitalSource,
    VitalTrend,
)
from app.services.lab_service import create_records_from_confirmed_lab
from app.services.vital_service import create_vital, create_vitals, get_vital_trends, list_vitals

router = APIRouter(prefix="/vitals")


@router.post("/confirm-extracted", response_model=ConfirmExtractedLabResponse)
async def confirm_extracted_lab(
    payload: ConfirmExtractedLabRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session),
) -> ConfirmExtractedLabResponse:
    biomarkers, vital_records = await create_records_from_confirmed_lab(
        db,
        current_user.id,
        payload.job_id,
    )
    return ConfirmExtractedLabResponse(
        biomarkers=biomarkers,
        vital_records=vital_records,
    )


@router.post("", response_model=VitalResponse, status_code=status.HTTP_201_CREATED)
async def post_vital(
    payload: VitalCreate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session),
) -> VitalResponse:
    return await create_vital(db, current_user.id, payload)


@router.post("/bulk-sync", response_model=list[VitalResponse], status_code=status.HTTP_201_CREATED)
async def post_vitals_bulk(
    payload: VitalBulkCreate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session),
) -> list[VitalResponse]:
    return await create_vitals(db, current_user.id, payload.records)


@router.get("", response_model=list[VitalResponse])
async def get_vitals(
    metric_type: VitalMetricType | None = None,
    start_date: datetime | None = None,
    end_date: datetime | None = None,
    source: VitalSource | None = None,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session),
) -> list[VitalResponse]:
    return await list_vitals(db, current_user.id, metric_type, start_date, end_date, source)


@router.get("/trends", response_model=list[VitalTrend])
async def get_trends(
    metric_type: VitalMetricType | None = None,
    start_date: datetime | None = Query(default=None),
    end_date: datetime | None = Query(default=None),
    source: VitalSource | None = None,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session),
) -> list[VitalTrend]:
    return await get_vital_trends(
        db,
        current_user.id,
        metric_type,
        start_date,
        end_date,
        source,
    )
