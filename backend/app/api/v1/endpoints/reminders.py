from fastapi import APIRouter, Depends, Query, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.api.deps import get_current_user
from app.db.session import get_db_session
from app.models.user import User
from app.schemas.reminder import AdherenceSummaryResponse, ReminderLogCreate, ReminderLogResponse
from app.services.reminder_service import (
    create_reminder_log,
    get_adherence_summary,
    list_reminder_logs,
)

router = APIRouter(prefix="/reminders")


@router.get("", response_model=list[ReminderLogResponse])
async def get_reminders(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session),
) -> list[ReminderLogResponse]:
    return await list_reminder_logs(db, current_user.id)


@router.post("/log", response_model=ReminderLogResponse, status_code=status.HTTP_201_CREATED)
async def post_reminder_log(
    payload: ReminderLogCreate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session),
) -> ReminderLogResponse:
    return await create_reminder_log(db, current_user.id, payload)


@router.get("/adherence-summary", response_model=AdherenceSummaryResponse)
async def get_adherence(
    days: int = Query(default=7, ge=1, le=365),
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session),
) -> AdherenceSummaryResponse:
    return await get_adherence_summary(db, current_user.id, days)
