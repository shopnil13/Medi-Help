from abc import ABC, abstractmethod

import httpx

from app.core.config import get_settings
from app.schemas.simplification import BiomarkerSimplification, MedicineSimplification

SIMPLIFICATION_PROMPT = """
Write health information that a 10-year-old can understand.
Use common words, active voice, and sentences under 20 words.
Keep each sentence focused on one idea.
Never diagnose the user or claim that a result proves a disease.
Never change, invent, or recommend a medicine dose.
Say when the information is uncertain and direct the user to a doctor or pharmacist.
Return only the requested structured JSON.
""".strip()

LAB_EXPLANATIONS = {
    "hba1c": (
        "HbA1c shows your average blood sugar over the past two to three months.",
        "Sugar sticks to hemoglobin in red blood cells. This test measures that amount.",
    ),
    "fasting_glucose": (
        "Fasting glucose measures sugar in your blood after you have not eaten.",
        "Your body uses blood sugar for energy. Food, medicine, and other factors can change it.",
    ),
    "blood_glucose": (
        "Blood glucose measures the sugar in your blood at the time of the test.",
        "Your body uses blood sugar for energy. The number can change during the day.",
    ),
    "ldl": (
        "LDL carries cholesterol in your blood.",
        "Too much LDL can build up inside blood vessels over time.",
    ),
    "hdl": (
        "HDL helps carry extra cholesterol away from your blood vessels.",
        "A higher HDL level is often better for heart health.",
    ),
    "triglycerides": (
        "Triglycerides are a type of fat found in your blood.",
        "Food, alcohol, medicines, and other factors can change this number.",
    ),
    "hemoglobin": (
        "Hemoglobin is a protein in red blood cells that carries oxygen.",
        "This test measures how much hemoglobin is in your blood.",
    ),
    "creatinine": (
        "Creatinine is a waste product made by your muscles.",
        "Kidneys usually remove it. Doctors often compare it with other information.",
    ),
}


class SimplificationProvider(ABC):
    @abstractmethod
    async def simplify_medicine(
        self,
        name: str,
        strength: str | None,
        instruction: str,
    ) -> MedicineSimplification:
        raise NotImplementedError

    @abstractmethod
    async def simplify_biomarker(
        self,
        name: str,
        normalized_name: str,
        status: str,
        reference_range: str | None,
    ) -> BiomarkerSimplification:
        raise NotImplementedError


class HeuristicSimplificationProvider(SimplificationProvider):
    async def simplify_medicine(
        self,
        name: str,
        strength: str | None,
        instruction: str,
    ) -> MedicineSimplification:
        del name, strength
        return MedicineSimplification(
            purpose=(
                "The prescription does not say what this medicine is for. "
                "Ask your doctor or pharmacist."
            ),
            how_to_take=f"Follow this instruction: {instruction}",
        )

    async def simplify_biomarker(
        self,
        name: str,
        normalized_name: str,
        status: str,
        reference_range: str | None,
    ) -> BiomarkerSimplification:
        explanation, details = LAB_EXPLANATIONS.get(
            normalized_name,
            (
                f"{name} is a measurement listed on your lab report.",
                "Your doctor can explain why this test was ordered and what it measures.",
            ),
        )
        return BiomarkerSimplification(
            explanation=explanation,
            status_meaning=_status_meaning(status, reference_range),
            more_details=details,
        )


class OpenAISimplificationProvider(SimplificationProvider):
    def __init__(self, api_key: str, api_url: str, model: str) -> None:
        if not api_key:
            raise ValueError("OPENAI_API_KEY is required for OpenAI simplification.")
        self.api_key = api_key
        self.api_url = api_url.rstrip("/")
        self.model = model

    async def simplify_medicine(
        self,
        name: str,
        strength: str | None,
        instruction: str,
    ) -> MedicineSimplification:
        return await self._request(
            MedicineSimplification,
            "medicine_simplification",
            (
                f"Medicine: {name}\nStrength: {strength or 'not provided'}\n"
                f"Confirmed instruction: {instruction}\n"
                "Explain its common purpose without assuming why this user takes it. "
                "Rewrite only the confirmed instruction without changing its meaning."
            ),
        )

    async def simplify_biomarker(
        self,
        name: str,
        normalized_name: str,
        status: str,
        reference_range: str | None,
    ) -> BiomarkerSimplification:
        return await self._request(
            BiomarkerSimplification,
            "biomarker_simplification",
            (
                f"Lab marker: {name}\nNormalized marker: {normalized_name}\n"
                f"Lab comparison: {status}\nReference range: {reference_range or 'not provided'}\n"
                "Explain the marker and the general comparison. Do not interpret a disease."
            ),
        )

    async def _request(self, schema_model, schema_name: str, user_content: str):
        payload = {
            "model": self.model,
            "messages": [
                {"role": "system", "content": SIMPLIFICATION_PROMPT},
                {"role": "user", "content": user_content},
            ],
            "response_format": {
                "type": "json_schema",
                "json_schema": {
                    "name": schema_name,
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
        return schema_model.model_validate_json(response.json()["choices"][0]["message"]["content"])


def _status_meaning(status: str, reference_range: str | None) -> str:
    range_suffix = f" of {reference_range}" if reference_range else " printed by the lab"
    if status == "normal":
        return f"This result is within the reference range{range_suffix}."
    if status == "high":
        return (
            f"This result is above the reference range{range_suffix}. "
            "One result cannot explain why."
        )
    if status == "low":
        return (
            f"This result is below the reference range{range_suffix}. "
            "One result cannot explain why."
        )
    return "The report does not provide enough information to compare this result with a range."


def get_simplification_provider() -> SimplificationProvider:
    settings = get_settings()
    if settings.simplification_backend == "heuristic":
        return HeuristicSimplificationProvider()
    if settings.simplification_backend == "openai":
        return OpenAISimplificationProvider(
            api_key=settings.openai_api_key or "",
            api_url=settings.openai_api_url,
            model=settings.openai_model,
        )
    raise ValueError(f"Unsupported simplification backend: {settings.simplification_backend}")
