package com.medihelp.app.feature_auth.domain.repository

import com.medihelp.app.core.common.Result

interface AuthRepository {
    suspend fun register(fullName: String, email: String, password: String): Result<String>
    suspend fun login(email: String, password: String): Result<String>
    suspend fun logout()
    fun isLoggedIn(): Boolean
}
