from typing import Annotated

from fastapi import APIRouter, Depends, File, Form, UploadFile, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.api.deps import get_current_user
from app.core.config import Settings, get_settings
from app.db.session import get_db_session
from app.models.user import User
from app.schemas.document import DocumentResponse, DocumentUploadResponse, ProcessingJobResponse
from app.services.document_service import upload_document
from app.storage import ObjectStorage, get_object_storage
from app.worker.dispatcher import JobDispatcher, get_job_dispatcher

router = APIRouter(prefix="/documents")


@router.post(
    "/upload",
    response_model=DocumentUploadResponse,
    status_code=status.HTTP_201_CREATED,
)
async def post_document_upload(
    document_type: Annotated[str, Form()],
    file: Annotated[UploadFile, File()],
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session),
    storage: ObjectStorage = Depends(get_object_storage),
    settings: Settings = Depends(get_settings),
    dispatcher: JobDispatcher = Depends(get_job_dispatcher),
) -> DocumentUploadResponse:
    job = await upload_document(
        db=db,
        storage=storage,
        user_id=current_user.id,
        document_type=document_type,
        upload=file,
        max_size_bytes=settings.max_upload_size_mb * 1024 * 1024,
    )
    dispatcher.enqueue(job.id)
    return DocumentUploadResponse(
        document=DocumentResponse.model_validate(job.document),
        job=ProcessingJobResponse.model_validate(job),
    )
