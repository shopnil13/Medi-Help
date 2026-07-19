from datetime import datetime
from typing import Literal
from uuid import UUID

from pydantic import BaseModel, ConfigDict, Field, model_validator

VitalMetricType = Literal[
    "heart_rate",
    "blood_pressure_systolic",
    "blood_pressure_diastolic",
    "blood_glucose",
    "weight",
    "custom",
]
VitalSource = Literal["manual", "lab_report", "health_connect", "device", "backend_import"]


class VitalCreate(BaseModel):
    metric_type: VitalMetricType
    metric_name: str | None = Field(default=None, min_length=1, max_length=120)
    value_numeric: float = Field(ge=-1_000_000, le=1_000_000)
    unit: str = Field(min_length=1, max_length=30)
    recorded_at: datetime
    source: VitalSource = "manual"
    source_document_id: UUID | None = None
    notes: str | None = Field(default=None, max_length=1000)

    @model_validator(mode="after")
    def validate_provenance(self) -> "VitalCreate":
        if self.source == "lab_report" and self.source_document_id is None:
            raise ValueError("Lab report records require a source document.")
        if self.source != "lab_report" and self.source_document_id is not None:
            raise ValueError("Source documents are only valid for lab report records.")
        return self


class VitalBulkCreate(BaseModel):
    records: list[VitalCreate] = Field(min_length=1, max_length=500)


class VitalResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: UUID
    user_id: UUID
    metric_type: VitalMetricType
    metric_name: str
    value_numeric: float
    unit: str
    recorded_at: datetime
    source: VitalSource
    source_document_id: UUID | None
    source_job_id: UUID | None
    notes: str | None
    created_at: datetime


class VitalTrend(BaseModel):
    metric_type: VitalMetricType
    metric_name: str
    unit: str
    count: int
    minimum: float
    maximum: float
    average: float
    latest: float
    direction: Literal["up", "down", "stable"]
    points: list[VitalResponse]


class ConfirmExtractedLabRequest(BaseModel):
    job_id: UUID


class BiomarkerResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: UUID
    source_document_id: UUID
    source_job_id: UUID | None
    name: str
    normalized_name: str
    value_numeric: float | None
    value_text: str | None
    unit: str | None
    reference_range_text: str | None
    status: Literal["low", "normal", "high", "unknown"]
    recorded_at: datetime
    confidence_score: float | None
    created_at: datetime


class ConfirmExtractedLabResponse(BaseModel):
    biomarkers: list[BiomarkerResponse]
    vital_records: list[VitalResponse]
