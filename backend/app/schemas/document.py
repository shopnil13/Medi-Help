from datetime import datetime
from typing import Literal
from uuid import UUID

from pydantic import BaseModel, ConfigDict, Field

from app.schemas.extraction import StructuredExtraction

DocumentType = Literal["prescription", "lab_report", "unknown"]
DocumentStatus = Literal["uploaded", "processing", "processed", "failed"]
JobStatus = Literal["queued", "processing", "needs_review", "completed", "failed"]


class DocumentResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: UUID
    document_type: DocumentType
    original_filename: str
    content_type: str
    file_size_bytes: int
    status: DocumentStatus
    created_at: datetime


class ProcessingJobResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True, populate_by_name=True)

    id: UUID
    document_id: UUID
    status: JobStatus
    progress_percent: int
    error_message: str | None
    created_at: datetime
    updated_at: datetime
    completed_at: datetime | None
    confirmed_at: datetime | None
    structured_result: StructuredExtraction | None = Field(
        default=None,
        validation_alias="structured_result_json",
    )
    confirmed_result: StructuredExtraction | None = Field(
        default=None,
        validation_alias="confirmed_result_json",
    )
    document: DocumentResponse


class DocumentUploadResponse(BaseModel):
    document: DocumentResponse
    job: ProcessingJobResponse
