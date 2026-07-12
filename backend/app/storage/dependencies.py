from functools import lru_cache

from app.core.config import get_settings
from app.storage.base import ObjectStorage
from app.storage.local import LocalObjectStorage
from app.storage.s3 import S3ObjectStorage


@lru_cache
def get_object_storage() -> ObjectStorage:
    settings = get_settings()
    if settings.storage_backend == "local":
        return LocalObjectStorage(settings.local_storage_path)
    if settings.storage_backend in {"s3", "minio"}:
        return S3ObjectStorage(
            endpoint_url=settings.s3_endpoint_url,
            access_key=settings.s3_access_key,
            secret_key=settings.s3_secret_key,
            bucket=settings.s3_bucket,
            region=settings.s3_region,
        )
    raise ValueError(f"Unsupported storage backend: {settings.storage_backend}")
