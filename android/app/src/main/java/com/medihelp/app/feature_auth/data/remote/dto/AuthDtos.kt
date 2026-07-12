package com.medihelp.app.feature_auth.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDto(
    @SerialName("full_name") val fullName: String,
    val email: String,
    val password: String,
)

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,
)

@Serializable
data class RefreshRequestDto(
    @SerialName("refresh_token") val refreshToken: String,
)

@Serializable
data class LogoutRequestDto(
    @SerialName("refresh_token") val refreshToken: String,
)

@Serializable
data class AuthTokenResponseDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("token_type") val tokenType: String = "bearer",
)

@Serializable
data class UserResponseDto(
    val id: String,
    @SerialName("full_name") val fullName: String,
    val email: String,
    @SerialName("is_active") val isActive: Boolean,
    @SerialName("is_verified") val isVerified: Boolean,
    @SerialName("created_at") val createdAt: String,
)
