from io import BytesIO
from uuid import UUID

import fitz
import pytest
import pytest_asyncio
from httpx import AsyncClient
from PIL import Image
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.main import app
from app.models.processing_job import ProcessingJob
from app.processing.extraction import ExtractionProvider, HeuristicExtractionProvider
from app.processing.image_processor import document_to_preprocessed_images
from app.processing.ocr import OCRProvider
from app.processing.pipeline import process_document_job
from app.processing.safety import apply_safety_flags
from app.schemas.extraction import ExtractedMedication, PrescriptionExtraction, StructuredExtraction
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

    async def get(self, key: str) -> bytes:
        return self.objects[key]


class FixedOCR(OCRProvider):
    async def extract_text(self, images: list[bytes]) -> str:
        assert images
        return "Metformin 500 mg twice daily"


class FixedExtractor(ExtractionProvider):
    async def extract(self, document_type: str, raw_text: str) -> StructuredExtraction:
        assert document_type == "prescription"
        assert "Metformin" in raw_text
        return PrescriptionExtraction(
            medications=[
                ExtractedMedication(
                    name="Metformin",
                    strength="500 mg",
                    dosage="1 tablet",
                    frequency="twice daily",
                    times=["08:00", "20:00"],
                    confidence=0.92,
                )
            ],
            overall_confidence=0.92,
        )


@pytest_asyncio.fixture
async def storage() -> MemoryStorage:
    value = MemoryStorage()
    app.dependency_overrides[get_object_storage] = lambda: value
    return value


def png_bytes() -> bytes:
    output = BytesIO()
    Image.new("RGB", (120, 60), "white").save(output, format="PNG")
    return output.getvalue()


async def register_and_upload(
    client: AsyncClient,
    storage: MemoryStorage,
) -> tuple[dict[str, str], str]:
    register = await client.post(
        "/api/v1/auth/register",
        json={"full_name": "OCR User", "email": "ocr@example.com", "password": "password123"},
    )
    headers = {"Authorization": f"Bearer {register.json()['access_token']}"}
    upload = await client.post(
        "/api/v1/documents/upload",
        headers=headers,
        data={"document_type": "prescription"},
        files={"file": ("prescription.png", png_bytes(), "image/png")},
    )
    assert upload.status_code == 201
    assert storage.objects
    return headers, upload.json()["job"]["id"]


async def test_pipeline_extracts_flags_and_allows_confirmation(
    client: AsyncClient,
    db_session: AsyncSession,
    storage: MemoryStorage,
) -> None:
    headers, job_id = await register_and_upload(client, storage)

    await process_document_job(
        db=db_session,
        job_id=UUID(job_id),
        storage=storage,
        ocr_provider=FixedOCR(),
        extraction_provider=FixedExtractor(),
    )

    status_response = await client.get(f"/api/v1/jobs/{job_id}", headers=headers)
    body = status_response.json()
    assert body["status"] == "needs_review"
    assert body["progress_percent"] == 100
    assert body["structured_result"]["medications"][0]["name"] == "Metformin"
    assert body["confirmed_result"] is None
    stored_job = await db_session.scalar(
        select(ProcessingJob).where(ProcessingJob.id == UUID(job_id))
    )
    assert stored_job is not None
    assert stored_job.raw_text == "Metformin 500 mg twice daily"

    confirmation = await client.post(
        f"/api/v1/jobs/{job_id}/confirm",
        headers=headers,
        json={"result": body["structured_result"]},
    )
    assert confirmation.status_code == 200
    assert confirmation.json()["status"] == "completed"
    assert confirmation.json()["confirmed_result"]["requires_confirmation"] is True
    repeated_confirmation = await client.post(
        f"/api/v1/jobs/{job_id}/confirm",
        headers=headers,
        json={"result": body["structured_result"]},
    )
    assert repeated_confirmation.status_code == 200
    assert (
        repeated_confirmation.json()["confirmed_result"] == confirmation.json()["confirmed_result"]
    )
    medications = await client.get("/api/v1/medications", headers=headers)
    assert medications.json() == []

    created = await client.post(
        "/api/v1/medications/confirm-extracted",
        headers=headers,
        json={"job_id": job_id},
    )
    assert created.status_code == 200
    created_body = created.json()
    assert len(created_body) == 1
    assert created_body[0]["name"] == "Metformin"
    assert created_body[0]["source_document_id"] == body["document_id"]
    assert created_body[0]["source_job_id"] == job_id
    assert len(created_body[0]["schedules"]) == 2
    assert created_body[0]["simplified_instruction"] == "1 tablet. twice daily"

    repeated = await client.post(
        "/api/v1/medications/confirm-extracted",
        headers=headers,
        json={"job_id": job_id},
    )
    assert [item["id"] for item in repeated.json()] == [item["id"] for item in created_body]


async def test_safety_flags_missing_frequency_and_low_confidence() -> None:
    result = PrescriptionExtraction(
        medications=[ExtractedMedication(name="UnclearMed", confidence=0.4)],
        overall_confidence=0.4,
    )

    flagged = apply_safety_flags(result)

    assert "Frequency is missing." in flagged.medications[0].warnings
    assert "Dosage is missing." in flagged.medications[0].warnings
    assert flagged.requires_confirmation is True


async def test_heuristic_extractor_keeps_result_in_review_shape() -> None:
    result = await HeuristicExtractionProvider().extract(
        "prescription",
        "Metformin 500 mg twice daily\nVitamin D 1000 IU once daily",
    )

    assert isinstance(result, PrescriptionExtraction)
    assert len(result.medications) == 2
    assert result.requires_confirmation is True


async def test_pdf_pages_are_converted_and_preprocessed() -> None:
    document = fitz.open()
    page = document.new_page(width=200, height=100)
    page.insert_text((20, 50), "Glucose 95 mg/dL")
    pdf = document.tobytes()
    document.close()

    images = document_to_preprocessed_images(pdf, "application/pdf")

    assert len(images) == 1
    assert images[0].startswith(b"\x89PNG\r\n\x1a\n")
