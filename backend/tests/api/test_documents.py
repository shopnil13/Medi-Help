from collections.abc import AsyncGenerator

import pytest
import pytest_asyncio
from httpx import AsyncClient

from app.core.config import Settings, get_settings
from app.main import app
from app.storage import ObjectStorage, StoredObject, get_object_storage

pytestmark = pytest.mark.asyncio


class MemoryStorage(ObjectStorage):
    def __init__(self) -> None:
        self.objects: dict[str, bytes] = {}

    async def put(self, key: str, data: bytes, content_type: str) -> StoredObject:
        del content_type
        self.objects[key] = data
        return StoredObject(provider="memory", key=key)

    async def delete(self, key: str) -> None:
        self.objects.pop(key, None)


@pytest_asyncio.fixture
async def storage() -> AsyncGenerator[MemoryStorage, None]:
    value = MemoryStorage()
    app.dependency_overrides[get_object_storage] = lambda: value
    yield value
    app.dependency_overrides.pop(get_object_storage, None)


@pytest_asyncio.fixture
async def auth_headers(client: AsyncClient) -> dict[str, str]:
    response = await client.post(
        "/api/v1/auth/register",
        json={"full_name": "Doc User", "email": "doc@example.com", "password": "password123"},
    )
    return {"Authorization": f"Bearer {response.json()['access_token']}"}


async def test_upload_creates_document_and_queued_job(
    client: AsyncClient,
    auth_headers: dict[str, str],
    storage: MemoryStorage,
) -> None:
    response = await client.post(
        "/api/v1/documents/upload",
        headers=auth_headers,
        data={"document_type": "prescription"},
        files={"file": ("prescription.pdf", b"%PDF-1.7\ncontent", "application/pdf")},
    )

    assert response.status_code == 201
    body = response.json()
    assert body["document"]["document_type"] == "prescription"
    assert body["job"]["status"] == "queued"
    assert body["job"]["document"]["id"] == body["document"]["id"]
    assert list(storage.objects.values()) == [b"%PDF-1.7\ncontent"]

    job_response = await client.get(f"/api/v1/jobs/{body['job']['id']}", headers=auth_headers)
    assert job_response.status_code == 200
    assert job_response.json()["progress_percent"] == 0


async def test_upload_requires_auth(client: AsyncClient, storage: MemoryStorage) -> None:
    response = await client.post(
        "/api/v1/documents/upload",
        data={"document_type": "lab_report"},
        files={"file": ("report.png", b"\x89PNG\r\n\x1a\ncontent", "image/png")},
    )
    assert response.status_code in (401, 403)


async def test_upload_rejects_unsupported_or_spoofed_files(
    client: AsyncClient,
    auth_headers: dict[str, str],
    storage: MemoryStorage,
) -> None:
    unsupported = await client.post(
        "/api/v1/documents/upload",
        headers=auth_headers,
        data={"document_type": "unknown"},
        files={"file": ("notes.txt", b"hello", "text/plain")},
    )
    spoofed = await client.post(
        "/api/v1/documents/upload",
        headers=auth_headers,
        data={"document_type": "unknown"},
        files={"file": ("fake.pdf", b"not a pdf", "application/pdf")},
    )
    assert unsupported.status_code == 415
    assert spoofed.status_code == 415
    assert storage.objects == {}


async def test_upload_rejects_files_over_configured_limit(
    client: AsyncClient,
    auth_headers: dict[str, str],
    storage: MemoryStorage,
) -> None:
    app.dependency_overrides[get_settings] = lambda: Settings(
        _env_file=None,
        debug=False,
        max_upload_size_mb=0,
    )
    try:
        response = await client.post(
            "/api/v1/documents/upload",
            headers=auth_headers,
            data={"document_type": "prescription"},
            files={"file": ("prescription.pdf", b"%PDF-1.7", "application/pdf")},
        )
    finally:
        app.dependency_overrides.pop(get_settings, None)

    assert response.status_code == 413
    assert storage.objects == {}


async def test_job_status_is_private_to_owner(
    client: AsyncClient,
    auth_headers: dict[str, str],
    storage: MemoryStorage,
) -> None:
    upload = await client.post(
        "/api/v1/documents/upload",
        headers=auth_headers,
        data={"document_type": "lab_report"},
        files={"file": ("report.jpg", b"\xff\xd8\xffcontent", "image/jpeg")},
    )
    other = await client.post(
        "/api/v1/auth/register",
        json={"full_name": "Other", "email": "other-doc@example.com", "password": "password123"},
    )
    response = await client.get(
        f"/api/v1/jobs/{upload.json()['job']['id']}",
        headers={"Authorization": f"Bearer {other.json()['access_token']}"},
    )
    assert response.status_code == 404
