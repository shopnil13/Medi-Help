package com.medihelp.app.feature_documents.data.mapper

import com.medihelp.app.feature_documents.data.remote.dto.DocumentResponseDto
import com.medihelp.app.feature_documents.data.remote.dto.ProcessingJobResponseDto
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class DocumentMappersTest {
    @Test
    fun jobResponseMapsToCachedDocumentAndDomainModel() {
        val dto = ProcessingJobResponseDto(
            id = "job-1",
            documentId = "document-1",
            status = "processing",
            progressPercent = 45,
            createdAt = "2026-07-13T10:00:00Z",
            updatedAt = "2026-07-13T10:01:00Z",
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

        val entity = dto.toEntity()
        val domain = entity.toDomain()

        assertEquals("job-1", entity.jobId)
        assertEquals(45, domain.progressPercent)
        assertEquals("prescription.pdf", domain.originalFilename)
        assertEquals(Instant.parse("2026-07-13T10:01:00Z"), domain.updatedAt)
    }
}
