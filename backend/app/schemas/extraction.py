from typing import Annotated, Literal

from pydantic import BaseModel, Field, TypeAdapter


class ExtractedMedication(BaseModel):
    name: str = Field(min_length=1, max_length=200)
    strength: str | None = Field(default=None, max_length=100)
    dosage: str | None = Field(default=None, max_length=200)
    frequency: str | None = Field(default=None, max_length=100)
    times: list[str] = Field(default_factory=list, max_length=12)
    duration: str | None = Field(default=None, max_length=100)
    meal_relation: str | None = Field(default=None, max_length=50)
    confidence: float = Field(ge=0, le=1)
    selected: bool = True
    warnings: list[str] = Field(default_factory=list)


class PrescriptionExtraction(BaseModel):
    document_type: Literal["prescription"] = "prescription"
    medications: list[ExtractedMedication]
    overall_confidence: float = Field(ge=0, le=1)
    requires_confirmation: Literal[True] = True
    warnings: list[str] = Field(default_factory=list)


class ExtractedBiomarker(BaseModel):
    name: str = Field(min_length=1, max_length=120)
    value: str = Field(min_length=1, max_length=100)
    unit: str | None = Field(default=None, max_length=30)
    reference_range: str | None = Field(default=None, max_length=100)
    confidence: float = Field(ge=0, le=1)
    selected: bool = True
    warnings: list[str] = Field(default_factory=list)


class LabReportExtraction(BaseModel):
    document_type: Literal["lab_report"] = "lab_report"
    biomarkers: list[ExtractedBiomarker]
    overall_confidence: float = Field(ge=0, le=1)
    requires_confirmation: Literal[True] = True
    warnings: list[str] = Field(default_factory=list)


StructuredExtraction = Annotated[
    PrescriptionExtraction | LabReportExtraction,
    Field(discriminator="document_type"),
]
structured_extraction_adapter: TypeAdapter[StructuredExtraction] = TypeAdapter(StructuredExtraction)


class ConfirmExtractionRequest(BaseModel):
    result: StructuredExtraction
