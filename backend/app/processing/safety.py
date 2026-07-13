from app.schemas.extraction import LabReportExtraction, PrescriptionExtraction, StructuredExtraction

LOW_CONFIDENCE_THRESHOLD = 0.75


def apply_safety_flags(result: StructuredExtraction) -> StructuredExtraction:
    if isinstance(result, PrescriptionExtraction):
        return _flag_prescription(result)
    return _flag_lab_report(result)


def _flag_prescription(result: PrescriptionExtraction) -> PrescriptionExtraction:
    flagged = result.model_copy(deep=True)
    for medication in flagged.medications:
        if medication.confidence < LOW_CONFIDENCE_THRESHOLD:
            medication.warnings.append("Medicine name or instruction needs careful review.")
        if not medication.dosage:
            medication.warnings.append("Dosage is missing.")
        if not medication.frequency:
            medication.warnings.append("Frequency is missing.")
    if any(item.warnings for item in flagged.medications):
        flagged.warnings.append("Review all flagged medicines before confirming.")
    return flagged


def _flag_lab_report(result: LabReportExtraction) -> LabReportExtraction:
    flagged = result.model_copy(deep=True)
    for biomarker in flagged.biomarkers:
        if biomarker.confidence < LOW_CONFIDENCE_THRESHOLD:
            biomarker.warnings.append("Biomarker name or value needs careful review.")
        if not biomarker.unit:
            biomarker.warnings.append("Unit is missing or unknown.")
    if any(item.warnings for item in flagged.biomarkers):
        flagged.warnings.append("Review all flagged lab values before confirming.")
    return flagged
