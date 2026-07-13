package com.medihelp.app.feature_documents.data.mapper

import com.medihelp.app.feature_documents.data.remote.dto.DocumentResponseDto
import com.medihelp.app.feature_documents.data.remote.dto.ProcessingJobResponseDto
import com.medihelp.app.feature_documents.data.remote.dto.PrescriptionExtractionDto
import com.medihelp.app.feature_documents.data.remote.dto.ExtractedMedicationDto
import com.medihelp.app.feature_documents.domain.model.PrescriptionExtraction
import java.time.Instant
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class DocumentMappersTest {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        classDiscriminator = "document_type"
    }

    @Test
    fun jobResponseMapsToCachedDocumentAndDomainModel() {
        val dto = ProcessingJobResponseDto(
            id = "job-1",
            documentId = "document-1",
            status = "processing",
            progressPercent = 45,
            createdAt = "2026-07-13T10:00:00Z",
            updatedAt = "2026-07-13T10:01:00Z",
            structuredResult = PrescriptionExtractionDto(
                medications = listOf(
                    ExtractedMedicationDto(
                        name = "Metformin",
                        strength = "500 mg",
                        confidence = 0.91,
                    ),
                ),
                overallConfidence = 0.91,
            ),
            document = DocumentResponseDto(
                id = "document-1",
                documentType = "prescription",
                originalFilename = "prescription.pdf",
                contentType = "application/pdf",
                fileSizeBytes = 2048,
                status = "uploaded",
                createdAt = "2026-07-13T10:00:00Z",
            ),
        )

        val entity = dto.toEntity(json)
        val domain = entity.toDomain(json)

        assertEquals("job-1", entity.jobId)
        assertEquals(45, domain.progressPercent)
        assertEquals("prescription.pdf", domain.originalFilename)
        assertEquals(Instant.parse("2026-07-13T10:01:00Z"), domain.updatedAt)
        val extraction = domain.structuredResult as PrescriptionExtraction
        assertEquals("Metformin", extraction.medications.single().name)
    }
}
