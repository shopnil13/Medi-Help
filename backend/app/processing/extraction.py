import re
from abc import ABC, abstractmethod

import httpx

from app.core.config import get_settings
from app.processing.prompts import LAB_REPORT_EXTRACTION_PROMPT, PRESCRIPTION_EXTRACTION_PROMPT
from app.schemas.extraction import (
    ExtractedBiomarker,
    ExtractedMedication,
    LabReportExtraction,
    PrescriptionExtraction,
    StructuredExtraction,
)


class ExtractionProvider(ABC):
    @abstractmethod
    async def extract(self, document_type: str, raw_text: str) -> StructuredExtraction:
        raise NotImplementedError


class HeuristicExtractionProvider(ExtractionProvider):
    async def extract(self, document_type: str, raw_text: str) -> StructuredExtraction:
        resolved_type = _resolve_document_type(document_type, raw_text)
        if resolved_type == "lab_report":
            return self._extract_lab_report(raw_text)
        return self._extract_prescription(raw_text)

    def _extract_prescription(self, raw_text: str) -> PrescriptionExtraction:
        medications = []
        strength_pattern = re.compile(r"\b\d+(?:\.\d+)?\s*(?:mcg|mg|g|ml|iu)\b", re.I)
        for raw_line in raw_text.splitlines():
            line = " ".join(raw_line.split())
            strength_match = strength_pattern.search(line)
            if not strength_match:
                continue
            name = re.sub(r"^[\d.\-\s]+", "", line[: strength_match.start()]).strip(" :-")
            if not name:
                continue
            frequency = _find_frequency(line)
            medications.append(
                ExtractedMedication(
                    name=name[:200],
                    strength=strength_match.group(0),
                    dosage=line[:200],
                    frequency=frequency,
                    confidence=0.65 if frequency else 0.5,
                )
            )
        confidence = (
            sum(item.confidence for item in medications) / len(medications) if medications else 0
        )
        warnings = [] if medications else ["No medicine lines could be identified with confidence."]
        return PrescriptionExtraction(
            medications=medications,
            overall_confidence=confidence,
            warnings=warnings,
        )

    def _extract_lab_report(self, raw_text: str) -> LabReportExtraction:
        biomarkers = []
        pattern = re.compile(
            r"^(?P<name>[A-Za-z][A-Za-z0-9 ()/%+.-]{1,80}?)\s+"
            r"(?P<value>[<>]?\d+(?:\.\d+)?)\s*"
            r"(?P<unit>[A-Za-z%/µμ^0-9.-]+)?(?:\s+(?P<range>\d+(?:\.\d+)?\s*[-–]\s*\d+(?:\.\d+)?))?$"
        )
        for raw_line in raw_text.splitlines():
            match = pattern.match(" ".join(raw_line.split()))
            if not match:
                continue
            biomarkers.append(
                ExtractedBiomarker(
                    name=match.group("name").strip(),
                    value=match.group("value"),
                    unit=match.group("unit"),
                    reference_range=match.group("range"),
                    confidence=0.7 if match.group("unit") else 0.5,
                )
            )
        confidence = (
            sum(item.confidence for item in biomarkers) / len(biomarkers) if biomarkers else 0
        )
        warnings = [] if biomarkers else ["No biomarker rows could be identified with confidence."]
        return LabReportExtraction(
            biomarkers=biomarkers,
            overall_confidence=confidence,
            warnings=warnings,
        )


class OpenAIExtractionProvider(ExtractionProvider):
    def __init__(self, api_key: str, api_url: str, model: str) -> None:
        if not api_key:
            raise ValueError("OPENAI_API_KEY is required for OpenAI extraction.")
        self.api_key = api_key
        self.api_url = api_url.rstrip("/")
        self.model = model

    async def extract(self, document_type: str, raw_text: str) -> StructuredExtraction:
        resolved_type = _resolve_document_type(document_type, raw_text)
        schema_model = (
            LabReportExtraction if resolved_type == "lab_report" else PrescriptionExtraction
        )
        prompt = (
            LAB_REPORT_EXTRACTION_PROMPT
            if resolved_type == "lab_report"
            else PRESCRIPTION_EXTRACTION_PROMPT
        )
        payload = {
            "model": self.model,
            "messages": [
                {"role": "system", "content": prompt},
                {"role": "user", "content": raw_text},
            ],
            "response_format": {
                "type": "json_schema",
                "json_schema": {
                    "name": f"{resolved_type}_extraction",
                    "strict": True,
                    "schema": schema_model.model_json_schema(),
                },
            },
        }
        async with httpx.AsyncClient(timeout=90) as client:
            response = await client.post(
                f"{self.api_url}/chat/completions",
                headers={"Authorization": f"Bearer {self.api_key}"},
                json=payload,
            )
            response.raise_for_status()
        content = response.json()["choices"][0]["message"]["content"]
        return schema_model.model_validate_json(content)


def _resolve_document_type(document_type: str, raw_text: str) -> str:
    if document_type in {"prescription", "lab_report"}:
        return document_type
    lab_terms = {"reference range", "hemoglobin", "glucose", "cholesterol", "creatinine"}
    normalized = raw_text.lower()
    return "lab_report" if any(term in normalized for term in lab_terms) else "prescription"


def _find_frequency(line: str) -> str | None:
    normalized = line.lower()
    options = {
        "once daily": ("once daily", "once a day", "od"),
        "twice daily": ("twice daily", "twice a day", "bid", "bd"),
        "three times daily": ("three times daily", "three times a day", "tid", "tds"),
        "at night": ("at night", "nightly", "hs"),
    }
    for value, terms in options.items():
        if any(re.search(rf"\b{re.escape(term)}\b", normalized) for term in terms):
            return value
    return None


def get_extraction_provider() -> ExtractionProvider:
    settings = get_settings()
    if settings.extraction_backend == "heuristic":
        return HeuristicExtractionProvider()
    if settings.extraction_backend == "openai":
        return OpenAIExtractionProvider(
            api_key=settings.openai_api_key or "",
            api_url=settings.openai_api_url,
            model=settings.openai_model,
        )
    raise ValueError(f"Unsupported extraction backend: {settings.extraction_backend}")
