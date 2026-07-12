package com.medihelp.app.core.security

import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenStorage @Inject constructor(
    private val encryptedPreferences: SharedPreferences,
) {
    fun getAccessToken(): String? = encryptedPreferences.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = encryptedPreferences.getString(KEY_REFRESH_TOKEN, null)

    fun saveTokens(accessToken: String, refreshToken: String) {
        encryptedPreferences.edit {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
        }
    }

    fun clearTokens() {
        encryptedPreferences.edit {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
        }
    }

    fun isLoggedIn(): Boolean = getAccessToken() != null

    private companion object {
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
    }
}
