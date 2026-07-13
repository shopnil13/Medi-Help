from abc import ABC, abstractmethod
from functools import lru_cache
from uuid import UUID


class JobDispatcher(ABC):
    @abstractmethod
    def enqueue(self, job_id: UUID) -> None:
        raise NotImplementedError


class CeleryJobDispatcher(JobDispatcher):
    def enqueue(self, job_id: UUID) -> None:
        from app.worker.tasks import process_document_task

        process_document_task.delay(str(job_id))


@lru_cache
def get_job_dispatcher() -> JobDispatcher:
    return CeleryJobDispatcher()
