package com.medihelp.app.core.network

import com.medihelp.app.core.security.TokenStorage
import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor @Inject constructor(
    private val tokenStorage: TokenStorage,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        if (AUTH_ENDPOINT_SEGMENT in original.url.encodedPath) {
            return chain.proceed(original)
        }

        val accessToken = tokenStorage.getAccessToken() ?: return chain.proceed(original)

        val authenticated = original.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()

        return chain.proceed(authenticated)
    }

    private companion object {
        const val AUTH_ENDPOINT_SEGMENT = "/api/v1/auth/"
    }
}
