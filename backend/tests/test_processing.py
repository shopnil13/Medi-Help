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
from app.schemas.extraction import (
    ExtractedBiomarker,
    ExtractedMedication,
    LabReportExtraction,
    PrescriptionExtraction,
    StructuredExtraction,
)
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


class FixedLabOCR(OCRProvider):
    async def extract_text(self, images: list[bytes]) -> str:
        assert images
        return "HbA1c 6.2%, fasting glucose 110 mg/dL, LDL 130 mg/dL"


class FixedLabExtractor(ExtractionProvider):
    async def extract(self, document_type: str, raw_text: str) -> StructuredExtraction:
        assert document_type == "lab_report"
        assert "HbA1c" in raw_text
        return LabReportExtraction(
            biomarkers=[
                ExtractedBiomarker(
                    name="HbA1c",
                    value="6.2",
                    unit="%",
                    reference_range="4.0-5.6",
                    confidence=0.96,
                ),
                ExtractedBiomarker(
                    name="Fasting Blood Glucose",
                    value="110",
                    unit="mg/dL",
                    reference_range="70-99",
                    confidence=0.95,
                ),
                ExtractedBiomarker(
                    name="LDL Cholesterol",
                    value="130",
                    unit="mg/dL",
                    reference_range="<100",
                    confidence=0.94,
                ),
                ExtractedBiomarker(
                    name="HDL Cholesterol",
                    value="55",
                    unit="mg/dL",
                    reference_range=">40",
                    confidence=0.93,
                ),
                ExtractedBiomarker(
                    name="Triglycerides",
                    value="150",
                    unit="mg/dL",
                    reference_range="<150",
                    confidence=0.92,
                ),
                ExtractedBiomarker(
                    name="Haemoglobin",
                    value="13.5",
                    unit="g/dL",
                    reference_range="12-16",
                    confidence=0.91,
                ),
                ExtractedBiomarker(
                    name="Serum Creatinine",
                    value="1.1",
                    unit="mg/dL",
                    reference_range="0.7-1.3",
                    confidence=0.9,
                ),
                ExtractedBiomarker(
                    name="Ketones",
                    value="Negative",
                    reference_range="Negative",
                    confidence=0.89,
                ),
                ExtractedBiomarker(
                    name="Not selected",
                    value="1",
                    unit="unit",
                    confidence=0.8,
                    selected=False,
                ),
            ],
            overall_confidence=0.93,
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
    document_type: str = "prescription",
) -> tuple[dict[str, str], str]:
    register = await client.post(
        "/api/v1/auth/register",
        json={"full_name": "OCR User", "email": "ocr@example.com", "password": "password123"},
    )
    headers = {"Authorization": f"Bearer {register.json()['access_token']}"}
    upload = await client.post(
        "/api/v1/documents/upload",
        headers=headers,
        data={"document_type": document_type},
        files={"file": (f"{document_type}.png", png_bytes(), "image/png")},
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

    wrong_route = await client.post(
        "/api/v1/vitals/confirm-extracted",
        headers=headers,
        json={"job_id": job_id},
    )
    assert wrong_route.status_code == 422


async def test_confirmed_lab_routes_normalized_biomarkers_to_vitals(
    client: AsyncClient,
    db_session: AsyncSession,
    storage: MemoryStorage,
) -> None:
    headers, job_id = await register_and_upload(client, storage, "lab_report")
    await process_document_job(
        db=db_session,
        job_id=UUID(job_id),
        storage=storage,
        ocr_provider=FixedLabOCR(),
        extraction_provider=FixedLabExtractor(),
    )
    job_response = await client.get(f"/api/v1/jobs/{job_id}", headers=headers)
    job_body = job_response.json()
    assert job_body["status"] == "needs_review"

    confirmation = await client.post(
        f"/api/v1/jobs/{job_id}/confirm",
        headers=headers,
        json={"result": job_body["structured_result"]},
    )
    assert confirmation.status_code == 200
    before_routing = await client.get("/api/v1/vitals", headers=headers)
    assert before_routing.json() == []

    routed = await client.post(
        "/api/v1/vitals/confirm-extracted",
        headers=headers,
        json={"job_id": job_id},
    )
    assert routed.status_code == 200
    body = routed.json()
    assert len(body["biomarkers"]) == 8
    assert len(body["vital_records"]) == 7
    by_name = {item["normalized_name"]: item for item in body["biomarkers"]}
    assert set(by_name) == {
        "hba1c",
        "fasting_glucose",
        "ldl",
        "hdl",
        "triglycerides",
        "hemoglobin",
        "creatinine",
        "ketones",
    }
    assert by_name["fasting_glucose"]["status"] == "high"
    assert by_name["fasting_glucose"]["unit"] == "mg/dL"
    assert by_name["fasting_glucose"]["reference_range_text"] == "70-99"
    assert by_name["fasting_glucose"]["source_document_id"] == job_body["document_id"]
    assert by_name["fasting_glucose"]["source_job_id"] == job_id
    assert by_name["ketones"]["value_numeric"] is None

    fasting_vital = next(
        item for item in body["vital_records"] if item["metric_name"] == "Fasting glucose"
    )
    assert fasting_vital["metric_type"] == "blood_glucose"
    assert fasting_vital["source"] == "lab_report"
    assert fasting_vital["source_document_id"] == job_body["document_id"]
    assert fasting_vital["source_job_id"] == job_id
    assert fasting_vital["source_biomarker_id"] == by_name["fasting_glucose"]["id"]
    assert fasting_vital["notes"] == "Reference range: 70-99"

    simplified = await client.post(
        f"/api/v1/vitals/biomarkers/{fasting_vital['source_biomarker_id']}/simplify",
        headers=headers,
    )
    assert simplified.status_code == 200
    assert "blood" in simplified.json()["explanation_simplified"].lower()
    assert "above" in simplified.json()["status_explanation"].lower()
    assert simplified.json()["ask_doctor"] is True

    cached = await client.post(
        f"/api/v1/vitals/biomarkers/{fasting_vital['source_biomarker_id']}/simplify",
        headers=headers,
    )
    assert cached.json()["details_simplified"] == simplified.json()["details_simplified"]

    other_registration = await client.post(
        "/api/v1/auth/register",
        json={
            "full_name": "Other Lab User",
            "email": "other-lab@example.com",
            "password": "password123",
        },
    )
    other_headers = {"Authorization": f"Bearer {other_registration.json()['access_token']}"}
    not_owned = await client.post(
        f"/api/v1/vitals/biomarkers/{fasting_vital['source_biomarker_id']}/simplify",
        headers=other_headers,
    )
    assert not_owned.status_code == 404

    repeated = await client.post(
        "/api/v1/vitals/confirm-extracted",
        headers=headers,
        json={"job_id": job_id},
    )
    assert [item["id"] for item in repeated.json()["biomarkers"]] == [
        item["id"] for item in body["biomarkers"]
    ]
    filtered = await client.get(
        "/api/v1/vitals",
        headers=headers,
        params={"source": "lab_report"},
    )
    assert len(filtered.json()) == 7


async def test_safety_flags_missing_frequency_and_low_confidence() -> None:
    result = PrescriptionExtraction(
        medications=[ExtractedMedication(name="UnclearMed", confidence=0.4)],
        overall_confidence=0.4,
    )

    flagged = apply_safety_flags(result)

    assert "Frequency is missing." in flagged.medications[0].warnings
    assert "Dosage is missing." in flagged.medications[0].warnings
    assert flagged.requires_confirmation is True


async def test_simplification_safety_removes_diagnosis_claims() -> None:
    from app.processing.simplification_safety import validate_biomarker_simplification
    from app.schemas.simplification import BiomarkerSimplification

    safe, changed = validate_biomarker_simplification(
        BiomarkerSimplification(
            explanation="This test measures blood sugar. You have diabetes.",
            status_meaning="This means you have a disease. One result cannot explain why.",
            more_details="Ask your doctor about this result.",
        )
    )

    assert changed is True
    assert "you have" not in " ".join(safe.model_dump().values()).lower()
    assert safe.explanation == "This test measures blood sugar."


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
