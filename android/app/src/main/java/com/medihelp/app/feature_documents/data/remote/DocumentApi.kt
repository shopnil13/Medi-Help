package com.medihelp.app.feature_documents.data.remote

import com.medihelp.app.feature_documents.data.remote.dto.DocumentUploadResponseDto
import com.medihelp.app.feature_documents.data.remote.dto.ProcessingJobResponseDto
import com.medihelp.app.feature_documents.data.remote.dto.ConfirmExtractionRequestDto
import retrofit2.http.Body
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface DocumentApi {
    @Multipart
    @POST("api/v1/documents/upload")
    suspend fun uploadDocument(
        @Part("document_type") documentType: RequestBody,
        @Part file: MultipartBody.Part,
    ): DocumentUploadResponseDto

    @GET("api/v1/jobs/{jobId}")
    suspend fun getJob(@Path("jobId") jobId: String): ProcessingJobResponseDto

    @POST("api/v1/jobs/{jobId}/confirm")
    suspend fun confirmExtraction(
        @Path("jobId") jobId: String,
        @Body body: ConfirmExtractionRequestDto,
    ): ProcessingJobResponseDto
}
