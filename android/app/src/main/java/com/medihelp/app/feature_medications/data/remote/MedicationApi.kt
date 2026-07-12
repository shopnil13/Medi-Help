package com.medihelp.app.feature_medications.data.remote

import com.medihelp.app.feature_medications.data.remote.dto.MedicationCreateRequestDto
import com.medihelp.app.feature_medications.data.remote.dto.MedicationResponseDto
import com.medihelp.app.feature_medications.data.remote.dto.MedicationUpdateRequestDto
import com.medihelp.app.feature_medications.data.remote.dto.ReminderLogRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface MedicationApi {

    @GET("api/v1/medications")
    suspend fun getMedications(): List<MedicationResponseDto>

    @POST("api/v1/medications")
    suspend fun createMedication(@Body body: MedicationCreateRequestDto): MedicationResponseDto

    @PATCH("api/v1/medications/{id}")
    suspend fun updateMedication(
        @Path("id") id: String,
        @Body body: MedicationUpdateRequestDto,
    ): MedicationResponseDto

    @DELETE("api/v1/medications/{id}")
    suspend fun deleteMedication(@Path("id") id: String)

    @POST("api/v1/reminders/log")
    suspend fun logReminder(@Body body: ReminderLogRequestDto)
}
