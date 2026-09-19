package com.medihelp.app.core.common

import android.util.Log
import com.medihelp.app.BuildConfig
import java.io.IOException
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import retrofit2.HttpException

@Serializable
private data class ApiErrorBody(val detail: String? = null)

private val errorJson = Json { ignoreUnknownKeys = true }

private const val TAG = "MediHelpError"

/**
 * Maps a throwable to reassuring, non-technical copy for the user.
 *
 * The user-facing string is deliberately vague, which makes failures hard to
 * diagnose from a screenshot alone — a blocked cleartext connection and a
 * genuinely offline device produce identical text. Debug builds therefore log
 * the underlying cause. Only the exception type and message are logged, never a
 * response body, since those can carry medical data.
 */
fun Throwable.toUserMessage(): String {
    if (BuildConfig.DEBUG) {
        Log.w(TAG, "Request failed: ${this::class.java.simpleName}: $message", this)
    }
    return toUserMessageInternal()
}

private fun Throwable.toUserMessageInternal(): String = when (this) {
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
