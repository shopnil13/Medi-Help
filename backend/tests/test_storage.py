import pytest

from app.storage.local import LocalObjectStorage

pytestmark = pytest.mark.asyncio


async def test_local_storage_writes_and_deletes_object(tmp_path) -> None:
    storage = LocalObjectStorage(str(tmp_path))

    stored = await storage.put("user/document.pdf", b"content", "application/pdf")

    assert stored.provider == "local"
    assert (tmp_path / "user" / "document.pdf").read_bytes() == b"content"
    assert await storage.get(stored.key) == b"content"

    await storage.delete(stored.key)
    assert not (tmp_path / "user" / "document.pdf").exists()


async def test_local_storage_rejects_path_traversal(tmp_path) -> None:
    storage = LocalObjectStorage(str(tmp_path))

    with pytest.raises(ValueError, match="escapes"):
        await storage.put("../outside.pdf", b"content", "application/pdf")
