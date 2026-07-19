package com.medihelp.app.core.navigation

object Routes {
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DASHBOARD = "dashboard"
    const val MEDICATIONS = "medications"
    const val ADD_MEDICATION = "add_medication"
    const val MEDICATION_DETAIL = "medication_detail/{medicationId}"
    const val VITALS = "vitals"
    const val ADD_VITAL = "add_vital"
    const val HEALTH_CONNECT = "health_connect"
    const val BIOMARKER_DETAIL = "biomarker_detail/{biomarkerId}"
    const val UPLOAD_DOCUMENT = "upload_document"
    const val DOCUMENT_CAMERA = "document_camera"
    const val PROCESSING_STATUS = "processing_status/{jobId}"
    const val EXTRACTION_REVIEW = "extraction_review/{jobId}"

    fun medicationDetail(medicationId: String) = "medication_detail/$medicationId"
    fun biomarkerDetail(biomarkerId: String) = "biomarker_detail/$biomarkerId"
    fun processingStatus(jobId: String) = "processing_status/$jobId"
    fun extractionReview(jobId: String) = "extraction_review/$jobId"
}
