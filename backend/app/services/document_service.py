from datetime import UTC, datetime
from pathlib import Path
from uuid import UUID, uuid4

from fastapi import HTTPException, UploadFile, status
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import selectinload

from app.models.document import Document
from app.models.processing_job import ProcessingJob
from app.schemas.extraction import StructuredExtraction
from app.storage.base import ObjectStorage

ALLOWED_FILES = {
    "application/pdf": ({".pdf"}, b"%PDF-"),
    "image/jpeg": ({".jpg", ".jpeg"}, b"\xff\xd8\xff"),
    "image/png": ({".png"}, b"\x89PNG\r\n\x1a\n"),
}
ALLOWED_DOCUMENT_TYPES = {"prescription", "lab_report", "unknown"}


async def upload_document(
    db: AsyncSession,
    storage: ObjectStorage,
    user_id: UUID,
    document_type: str,
    upload: UploadFile,
    max_size_bytes: int,
) -> ProcessingJob:
    if document_type not in ALLOWED_DOCUMENT_TYPES:
        raise HTTPException(status.HTTP_422_UNPROCESSABLE_ENTITY, "Invalid document type.")

    content_type = upload.content_type or ""
    filename = Path(upload.filename or "document").name
    extension = Path(filename).suffix.lower()
    allowed = ALLOWED_FILES.get(content_type)
    if allowed is None or extension not in allowed[0]:
        raise HTTPException(
            status.HTTP_415_UNSUPPORTED_MEDIA_TYPE,
            "Only PDF, JPEG, and PNG documents are supported.",
        )

    data = await upload.read(max_size_bytes + 1)
    await upload.close()
    if not data:
        raise HTTPException(status.HTTP_422_UNPROCESSABLE_ENTITY, "Uploaded file is empty.")
    if len(data) > max_size_bytes:
        raise HTTPException(status.HTTP_413_CONTENT_TOO_LARGE, "Uploaded file is too large.")
    if not data.startswith(allowed[1]):
        raise HTTPException(status.HTTP_415_UNSUPPORTED_MEDIA_TYPE, "File content is invalid.")

    document_id = uuid4()
    storage_key = f"{user_id}/{document_id}{extension}"
    stored = await storage.put(storage_key, data, content_type)
    try:
        document = Document(
            id=document_id,
            user_id=user_id,
            document_type=document_type,
            original_filename=filename,
            content_type=content_type,
            file_size_bytes=len(data),
            storage_provider=stored.provider,
            storage_key=stored.key,
        )
        job = ProcessingJob(document=document, user_id=user_id)
        db.add(job)
        await db.commit()
    except Exception:
        await db.rollback()
        await storage.delete(stored.key)
        raise

    return await get_processing_job(db, user_id, job.id)


async def get_processing_job(db: AsyncSession, user_id: UUID, job_id: UUID) -> ProcessingJob:
    stmt = (
        select(ProcessingJob)
        .options(selectinload(ProcessingJob.document))
        .where(ProcessingJob.id == job_id, ProcessingJob.user_id == user_id)
    )
    result = await db.execute(stmt)
    job = result.scalar_one_or_none()
    if job is None:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Processing job not found.")
    return job


async def confirm_extraction(
    db: AsyncSession,
    user_id: UUID,
    job_id: UUID,
    result: StructuredExtraction,
) -> ProcessingJob:
    job = await get_processing_job(db, user_id, job_id)
    if job.status == "completed" and job.confirmed_result_json is not None:
        return job
    if job.status != "needs_review" or job.structured_result_json is None:
        raise HTTPException(
            status.HTTP_409_CONFLICT, "This extraction is not ready for confirmation."
        )
    extracted_type = job.structured_result_json.get("document_type")
    if result.document_type != extracted_type:
        raise HTTPException(status.HTTP_422_UNPROCESSABLE_ENTITY, "Document type does not match.")

    job.confirmed_result_json = result.model_dump(mode="json")
    job.status = "completed"
    job.confirmed_at = datetime.now(UTC)
    job.completed_at = job.confirmed_at
    await db.commit()
    return await get_processing_job(db, user_id, job_id)
