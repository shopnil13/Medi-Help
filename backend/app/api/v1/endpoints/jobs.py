from uuid import UUID

from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.api.deps import get_current_user
from app.db.session import get_db_session
from app.models.user import User
from app.schemas.document import ProcessingJobResponse
from app.services.document_service import get_processing_job

router = APIRouter(prefix="/jobs")


@router.get("/{job_id}", response_model=ProcessingJobResponse)
async def get_job_status(
    job_id: UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session),
) -> ProcessingJobResponse:
    job = await get_processing_job(db, current_user.id, job_id)
    return ProcessingJobResponse.model_validate(job)
