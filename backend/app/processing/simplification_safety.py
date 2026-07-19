import re

from app.schemas.simplification import BiomarkerSimplification, MedicineSimplification

DIAGNOSIS_PATTERNS = (
    re.compile(
        r"\byou have (?:[a-z-]+\s+){0,3}"
        r"(?:disease|disorder|syndrome|diabetes|cancer|infection|anemia|hypertension)\b",
        re.I,
    ),
    re.compile(r"\byou (?:suffer from|are diagnosed with)\b", re.I),
    re.compile(r"\bthis (?:means|proves|confirms) (?:that )?you\b", re.I),
    re.compile(r"\bdefinitely (?:means|shows|proves)\b", re.I),
    re.compile(r"\bdiagnos(?:e|ed|is|ing)\b", re.I),
)


def validate_medicine_simplification(
    result: MedicineSimplification,
    original_instruction: str,
) -> tuple[MedicineSimplification, bool]:
    purpose, purpose_changed = _remove_diagnosis_claims(result.purpose)
    instruction, instruction_changed = _remove_diagnosis_claims(result.how_to_take)
    if _numbers(instruction) != _numbers(original_instruction):
        instruction = f"Follow this instruction: {original_instruction}"
        instruction_changed = True
    return (
        MedicineSimplification(
            purpose=purpose or "Ask your doctor or pharmacist what this medicine is for.",
            how_to_take=instruction or f"Follow this instruction: {original_instruction}",
        ),
        purpose_changed or instruction_changed,
    )


def validate_biomarker_simplification(
    result: BiomarkerSimplification,
) -> tuple[BiomarkerSimplification, bool]:
    explanation, explanation_changed = _remove_diagnosis_claims(result.explanation)
    status_meaning, status_changed = _remove_diagnosis_claims(result.status_meaning)
    details, details_changed = _remove_diagnosis_claims(result.more_details)
    return (
        BiomarkerSimplification(
            explanation=explanation or "Ask your doctor what this lab marker measures.",
            status_meaning=status_meaning or "One result cannot diagnose a health condition.",
            more_details=details or "Your doctor can explain this result with your health history.",
        ),
        explanation_changed or status_changed or details_changed,
    )


def _remove_diagnosis_claims(value: str) -> tuple[str, bool]:
    sentences = re.split(r"(?<=[.!?])\s+", " ".join(value.split()))
    safe = [
        sentence
        for sentence in sentences
        if not any(pattern.search(sentence) for pattern in DIAGNOSIS_PATTERNS)
    ]
    return " ".join(safe).strip(), len(safe) != len(sentences)


def _numbers(value: str) -> list[str]:
    return re.findall(r"\d+(?:\.\d+)?", value)
