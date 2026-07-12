package com.medihelp.app.core.network

import com.medihelp.app.BuildConfig
import com.medihelp.app.core.security.TokenStorage
import com.medihelp.app.feature_auth.data.remote.AuthApi
import com.medihelp.app.feature_auth.data.remote.dto.RefreshRequestDto
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.Retrofit
import retrofit2.create

/**
 * Handles a 401 by exchanging the refresh token for a new access token.
 *
 * Uses its own bare Retrofit/OkHttp instance (no [AuthInterceptor], no
 * authenticator) rather than the app-wide DI-provided one, since that one
 * depends on this authenticator — reusing it here would be circular.
 */
class TokenAuthenticator @Inject constructor(
    private val tokenStorage: TokenStorage,
) : Authenticator {

    private val refreshApi: AuthApi by lazy {
        val json = Json { ignoreUnknownKeys = true }
        val contentType = "application/json".toMediaType()
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create<AuthApi>()
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null

        val refreshToken = tokenStorage.getRefreshToken() ?: return null

        return try {
            val newTokens = runBlocking { refreshApi.refresh(RefreshRequestDto(refreshToken)) }
            tokenStorage.saveTokens(newTokens.accessToken, newTokens.refreshToken)
            response.request.newBuilder()
                .header("Authorization", "Bearer ${newTokens.accessToken}")
                .build()
        } catch (_: Exception) {
            tokenStorage.clearTokens()
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var prior = response.priorResponse
        while (prior != null) {
            result++
            prior = prior.priorResponse
        }
        return result
    }
}
