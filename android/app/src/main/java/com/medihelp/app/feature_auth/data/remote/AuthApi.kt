package com.medihelp.app.feature_auth.data.remote

import com.medihelp.app.feature_auth.data.remote.dto.AuthTokenResponseDto
import com.medihelp.app.feature_auth.data.remote.dto.LoginRequestDto
import com.medihelp.app.feature_auth.data.remote.dto.LogoutRequestDto
import com.medihelp.app.feature_auth.data.remote.dto.RefreshRequestDto
import com.medihelp.app.feature_auth.data.remote.dto.RegisterRequestDto
import com.medihelp.app.feature_auth.data.remote.dto.UserResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    @POST("api/v1/auth/register")
    suspend fun register(@Body body: RegisterRequestDto): AuthTokenResponseDto

    @POST("api/v1/auth/login")
    suspend fun login(@Body body: LoginRequestDto): AuthTokenResponseDto

    @POST("api/v1/auth/refresh")
    suspend fun refresh(@Body body: RefreshRequestDto): AuthTokenResponseDto

    @POST("api/v1/auth/logout")
    suspend fun logout(@Body body: LogoutRequestDto)

    @GET("api/v1/users/me")
    suspend fun getCurrentUser(): UserResponseDto
}
