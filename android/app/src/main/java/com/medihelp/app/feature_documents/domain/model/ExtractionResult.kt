package com.medihelp.app.feature_documents.domain.model

sealed interface ExtractionResult {
    val overallConfidence: Double
    val warnings: List<String>
}

data class PrescriptionExtraction(
    val medications: List<ExtractedMedication>,
    override val overallConfidence: Double,
    override val warnings: List<String> = emptyList(),
) : ExtractionResult

data class ExtractedMedication(
    val name: String,
    val strength: String? = null,
    val dosage: String? = null,
    val frequency: String? = null,
    val times: List<String> = emptyList(),
    val duration: String? = null,
    val mealRelation: String? = null,
    val confidence: Double,
    val selected: Boolean = true,
    val warnings: List<String> = emptyList(),
)

data class LabReportExtraction(
    val biomarkers: List<ExtractedBiomarker>,
    override val overallConfidence: Double,
    override val warnings: List<String> = emptyList(),
) : ExtractionResult

data class ExtractedBiomarker(
    val name: String,
    val value: String,
    val unit: String? = null,
    val referenceRange: String? = null,
    val confidence: Double,
    val selected: Boolean = true,
    val warnings: List<String> = emptyList(),
)
