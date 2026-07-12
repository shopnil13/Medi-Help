package com.medihelp.app.core.common

import java.io.IOException
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import retrofit2.HttpException

@Serializable
private data class ApiErrorBody(val detail: String? = null)

private val errorJson = Json { ignoreUnknownKeys = true }

fun Throwable.toUserMessage(): String = when (this) {
    is HttpException -> {
        val detail = errorBody()?.string()?.let { body ->
            runCatching { errorJson.decodeFromString(ApiErrorBody.serializer(), body) }
                .getOrNull()
                ?.detail
        }
        detail ?: when (code()) {
            401 -> "Your session has expired. Please log in again."
            409 -> "An account with this email already exists."
            in 500..599 -> "Something went wrong on our end. Please try again."
            else -> "Something went wrong. Please try again."
        }
    }
    is IOException -> "Please check your internet connection and try again."
    else -> message ?: "Something went wrong. Please try again."
}

private fun HttpException.errorBody() = response()?.errorBody()
