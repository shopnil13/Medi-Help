package com.medihelp.app.feature_vitals.data.remote

import com.medihelp.app.feature_vitals.data.remote.dto.VitalBulkCreateRequestDto
import com.medihelp.app.feature_vitals.data.remote.dto.ConfirmExtractedLabRequestDto
import com.medihelp.app.feature_vitals.data.remote.dto.ConfirmExtractedLabResponseDto
import com.medihelp.app.feature_vitals.data.remote.dto.VitalResponseDto
import com.medihelp.app.feature_vitals.data.remote.dto.BiomarkerResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface VitalApi {
    @POST("api/v1/vitals/biomarkers/{id}/simplify")
    suspend fun simplifyBiomarker(@Path("id") id: String): BiomarkerResponseDto

    @POST("api/v1/vitals/confirm-extracted")
    suspend fun confirmExtractedLab(
        @Body request: ConfirmExtractedLabRequestDto,
    ): ConfirmExtractedLabResponseDto

    @GET("api/v1/vitals")
    suspend fun getVitals(): List<VitalResponseDto>

    @POST("api/v1/vitals/bulk-sync")
    suspend fun bulkSync(@Body request: VitalBulkCreateRequestDto): List<VitalResponseDto>
}
