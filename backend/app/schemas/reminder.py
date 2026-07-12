from datetime import datetime
from typing import Literal
from uuid import UUID

from pydantic import BaseModel, ConfigDict

ReminderAction = Literal["taken", "skipped", "missed", "snoozed"]


class ReminderLogCreate(BaseModel):
    medication_id: UUID
    scheduled_at: datetime
    action: ReminderAction
    action_at: datetime | None = None


class ReminderLogResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: UUID
    user_id: UUID
    medication_id: UUID
    scheduled_at: datetime
    action: ReminderAction
    action_at: datetime | None
    created_at: datetime


class AdherenceSummaryResponse(BaseModel):
    taken_count: int
    skipped_count: int
    missed_count: int
    snoozed_count: int
    total_count: int
