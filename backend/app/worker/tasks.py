import asyncio
from typing import Any
from uuid import UUID

from app.db.session import AsyncSessionLocal, engine
from app.processing.extraction import get_extraction_provider
from app.processing.ocr import get_ocr_provider
from app.processing.pipeline import process_document_job
from app.storage import get_object_storage
from app.worker.celery_app import celery_app


@celery_app.task(  # type: ignore[untyped-decorator]
    bind=True,
    autoretry_for=(Exception,),
    retry_backoff=True,
    retry_jitter=True,
    max_retries=3,
)
def process_document_task(self: Any, job_id: str) -> None:
    del self
    asyncio.run(_process(UUID(job_id)))


async def _process(job_id: UUID) -> None:
    try:
        async with AsyncSessionLocal() as db:
            await process_document_job(
                db=db,
                job_id=job_id,
                storage=get_object_storage(),
                ocr_provider=get_ocr_provider(),
                extraction_provider=get_extraction_provider(),
            )
    finally:
        await engine.dispose()
