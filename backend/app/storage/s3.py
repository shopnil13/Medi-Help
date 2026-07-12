import asyncio
from typing import Any

import boto3
from botocore.exceptions import ClientError

from app.storage.base import ObjectStorage, StoredObject


class S3ObjectStorage(ObjectStorage):
    def __init__(
        self,
        endpoint_url: str,
        access_key: str,
        secret_key: str,
        bucket: str,
        region: str,
    ) -> None:
        self.bucket = bucket
        self.client: Any = boto3.client(
            "s3",
            endpoint_url=endpoint_url,
            aws_access_key_id=access_key,
            aws_secret_access_key=secret_key,
            region_name=region,
        )

    def _ensure_bucket(self) -> None:
        try:
            self.client.head_bucket(Bucket=self.bucket)
        except ClientError:
            self.client.create_bucket(Bucket=self.bucket)

    async def put(self, key: str, data: bytes, content_type: str) -> StoredObject:
        def upload() -> None:
            self._ensure_bucket()
            self.client.put_object(
                Bucket=self.bucket,
                Key=key,
                Body=data,
                ContentType=content_type,
            )

        await asyncio.to_thread(upload)
        return StoredObject(provider="s3", key=key)

    async def delete(self, key: str) -> None:
        await asyncio.to_thread(self.client.delete_object, Bucket=self.bucket, Key=key)
