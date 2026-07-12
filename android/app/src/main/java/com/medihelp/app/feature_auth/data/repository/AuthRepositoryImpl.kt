package com.medihelp.app.feature_auth.data.repository

import com.medihelp.app.core.common.Result
import com.medihelp.app.core.common.toUserMessage
import com.medihelp.app.core.datastore.UserPreferencesDataStore
import com.medihelp.app.core.security.TokenStorage
import com.medihelp.app.feature_auth.data.remote.AuthApi
import com.medihelp.app.feature_auth.data.remote.dto.LoginRequestDto
import com.medihelp.app.feature_auth.data.remote.dto.LogoutRequestDto
import com.medihelp.app.feature_auth.data.remote.dto.RegisterRequestDto
import com.medihelp.app.feature_auth.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage,
    private val userPreferencesDataStore: UserPreferencesDataStore,
) : AuthRepository {

    override suspend fun register(
        fullName: String,
        email: String,
        password: String,
    ): Result<String> = runCatching {
        val tokens = authApi.register(RegisterRequestDto(fullName, email, password))
        tokenStorage.saveTokens(tokens.accessToken, tokens.refreshToken)
        val user = authApi.getCurrentUser()
        userPreferencesDataStore.setDisplayName(user.fullName)
        user.fullName
    }.fold(
        onSuccess = { Result.Success(it) },
        onFailure = { Result.Error(it.toUserMessage()) },
    )

    override suspend fun login(email: String, password: String): Result<String> = runCatching {
        val tokens = authApi.login(LoginRequestDto(email, password))
        tokenStorage.saveTokens(tokens.accessToken, tokens.refreshToken)
        val user = authApi.getCurrentUser()
        userPreferencesDataStore.setDisplayName(user.fullName)
        user.fullName
    }.fold(
        onSuccess = { Result.Success(it) },
        onFailure = { Result.Error(it.toUserMessage()) },
    )

    override suspend fun logout() {
        val refreshToken = tokenStorage.getRefreshToken()
        if (refreshToken != null) {
            runCatching { authApi.logout(LogoutRequestDto(refreshToken)) }
        }
        tokenStorage.clearTokens()
        userPreferencesDataStore.clear()
    }

    override fun isLoggedIn(): Boolean = tokenStorage.isLoggedIn()
}
