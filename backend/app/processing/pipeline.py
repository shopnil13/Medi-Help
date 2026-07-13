from datetime import UTC, datetime
from uuid import UUID

from sqlalchemy import select, update
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import selectinload

from app.models.document import Document
from app.models.processing_job import ProcessingJob
from app.processing.extraction import ExtractionProvider
from app.processing.image_processor import document_to_preprocessed_images
from app.processing.ocr import OCRProvider
from app.processing.safety import apply_safety_flags
from app.schemas.extraction import structured_extraction_adapter
from app.storage.base import ObjectStorage


async def process_document_job(
    db: AsyncSession,
    job_id: UUID,
    storage: ObjectStorage,
    ocr_provider: OCRProvider,
    extraction_provider: ExtractionProvider,
) -> None:
    job = await _load_job(db, job_id)
    document_id = job.document_id
    try:
        await _set_progress(db, job, 10)
        data = await storage.get(job.document.storage_key)
        images = document_to_preprocessed_images(data, job.document.content_type)
        await _set_progress(db, job, 35)

        raw_text = (await ocr_provider.extract_text(images)).strip()
        if not raw_text:
            raise ValueError("OCR returned no text.")
        job.raw_text = raw_text
        await _set_progress(db, job, 65)

        extracted = await extraction_provider.extract(job.document.document_type, raw_text)
        validated = structured_extraction_adapter.validate_python(extracted.model_dump(mode="json"))
        safe_result = apply_safety_flags(validated)
        job.structured_result_json = safe_result.model_dump(mode="json")
        job.status = "needs_review"
        job.progress_percent = 100
        job.completed_at = datetime.now(UTC)
        job.document.status = "processed"
        await db.commit()
    except Exception:
        await db.rollback()
        await db.execute(
            update(ProcessingJob)
            .where(ProcessingJob.id == job_id)
            .values(
                status="failed",
                error_message="Document processing failed.",
                completed_at=datetime.now(UTC),
            )
        )
        await db.execute(update(Document).where(Document.id == document_id).values(status="failed"))
        await db.commit()
        raise


async def _load_job(db: AsyncSession, job_id: UUID) -> ProcessingJob:
    result = await db.execute(
        select(ProcessingJob)
        .options(selectinload(ProcessingJob.document))
        .where(ProcessingJob.id == job_id)
    )
    job = result.scalar_one_or_none()
    if job is None:
        raise ValueError("Processing job not found.")
    return job


async def _set_progress(db: AsyncSession, job: ProcessingJob, progress: int) -> None:
    job.status = "processing"
    job.progress_percent = progress
    job.error_message = None
    job.document.status = "processing"
    await db.commit()
