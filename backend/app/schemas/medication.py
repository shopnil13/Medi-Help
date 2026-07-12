from datetime import date, datetime, time
from typing import Literal
from uuid import UUID

from pydantic import BaseModel, ConfigDict, Field

FrequencyType = Literal["daily", "weekly", "custom"]
MealRelation = Literal["before_food", "after_food", "with_food", "no_relation", "unknown"]
MedicationStatus = Literal["active", "paused", "completed", "cancelled"]


class MedicationScheduleCreate(BaseModel):
    time_of_day: time
    frequency_type: FrequencyType = "daily"
    days_of_week: str | None = Field(default=None, max_length=20)
    meal_relation: MealRelation = "unknown"
    dose_amount: str | None = Field(default=None, max_length=50)
    notes: str | None = Field(default=None, max_length=300)


class MedicationScheduleResponse(MedicationScheduleCreate):
    model_config = ConfigDict(from_attributes=True)

    id: UUID
    medication_id: UUID
    created_at: datetime
    updated_at: datetime


class MedicationCreate(BaseModel):
    name: str = Field(min_length=1, max_length=200)
    strength: str | None = Field(default=None, max_length=100)
    dosage_instruction: str = Field(min_length=1, max_length=500)
    purpose_simplified: str | None = Field(default=None, max_length=500)
    start_date: date | None = None
    end_date: date | None = None
    schedules: list[MedicationScheduleCreate] = Field(default_factory=list)


class MedicationUpdate(BaseModel):
    name: str | None = Field(default=None, min_length=1, max_length=200)
    strength: str | None = Field(default=None, max_length=100)
    dosage_instruction: str | None = Field(default=None, min_length=1, max_length=500)
    simplified_instruction: str | None = Field(default=None, max_length=500)
    purpose_simplified: str | None = Field(default=None, max_length=500)
    start_date: date | None = None
    end_date: date | None = None
    status: MedicationStatus | None = None


class MedicationResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: UUID
    user_id: UUID
    name: str
    strength: str | None
    dosage_instruction: str
    simplified_instruction: str | None
    purpose_simplified: str | None
    start_date: date | None
    end_date: date | None
    status: MedicationStatus
    confidence_score: float | None
    requires_review: bool
    created_at: datetime
    updated_at: datetime
    schedules: list[MedicationScheduleResponse] = Field(default_factory=list)
