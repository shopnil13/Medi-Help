from datetime import UTC, datetime, timedelta
from uuid import UUID

from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.reminder_log import ReminderLog
from app.schemas.reminder import AdherenceSummaryResponse, ReminderLogCreate
from app.services.medication_service import get_medication


async def create_reminder_log(
    db: AsyncSession,
    user_id: UUID,
    payload: ReminderLogCreate,
) -> ReminderLog:
    # Confirms the medication exists and belongs to this user (404s otherwise).
    await get_medication(db, user_id, payload.medication_id)

    log = ReminderLog(
        user_id=user_id,
        medication_id=payload.medication_id,
        scheduled_at=payload.scheduled_at,
        action=payload.action,
        action_at=payload.action_at or datetime.now(UTC),
    )

    db.add(log)
    await db.commit()
    await db.refresh(log)

    return log


async def list_reminder_logs(db: AsyncSession, user_id: UUID) -> list[ReminderLog]:
    stmt = (
        select(ReminderLog)
        .where(ReminderLog.user_id == user_id)
        .order_by(ReminderLog.scheduled_at.desc())
    )
    result = await db.execute(stmt)
    return list(result.scalars().all())


async def get_adherence_summary(
    db: AsyncSession,
    user_id: UUID,
    days: int = 7,
) -> AdherenceSummaryResponse:
    since = datetime.now(UTC) - timedelta(days=days)

    stmt = (
        select(ReminderLog.action, func.count())
        .where(ReminderLog.user_id == user_id, ReminderLog.scheduled_at >= since)
        .group_by(ReminderLog.action)
    )
    result = await db.execute(stmt)
    counts = dict(result.all())

    taken = counts.get("taken", 0)
    skipped = counts.get("skipped", 0)
    missed = counts.get("missed", 0)
    snoozed = counts.get("snoozed", 0)

    return AdherenceSummaryResponse(
        taken_count=taken,
        skipped_count=skipped,
        missed_count=missed,
        snoozed_count=snoozed,
        total_count=taken + skipped + missed + snoozed,
    )
