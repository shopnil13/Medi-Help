import asyncio
from pathlib import Path

from app.storage.base import ObjectStorage, StoredObject


class LocalObjectStorage(ObjectStorage):
    def __init__(self, root: str) -> None:
        self.root = Path(root).resolve()

    def _target(self, key: str) -> Path:
        target = (self.root / key).resolve()
        if self.root not in target.parents:
            raise ValueError("Storage key escapes the configured root.")
        return target

    async def put(self, key: str, data: bytes, content_type: str) -> StoredObject:
        del content_type
        target = self._target(key)

        def write() -> None:
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(data)

        await asyncio.to_thread(write)
        return StoredObject(provider="local", key=key)

    async def delete(self, key: str) -> None:
        target = self._target(key)

        def remove() -> None:
            target.unlink(missing_ok=True)

        await asyncio.to_thread(remove)

    async def get(self, key: str) -> bytes:
        return await asyncio.to_thread(self._target(key).read_bytes)
