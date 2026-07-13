PRESCRIPTION_EXTRACTION_PROMPT = """
Extract prescribed medicines from the OCR text. Return only JSON matching the
provided schema. Do not infer a medicine, dose, or frequency that is not visible.
Use null for missing fields, confidence from 0 to 1, and add warnings for uncertain
medicine names or missing dosage/frequency. Data must remain inactive until confirmed.
""".strip()

LAB_REPORT_EXTRACTION_PROMPT = """
Extract biomarker results from the OCR text. Return only JSON matching the provided
schema. Preserve the printed value, unit, and reference range. Use null for missing
units, confidence from 0 to 1, and add warnings for uncertain names or unknown units.
Do not diagnose or interpret the result.
""".strip()
