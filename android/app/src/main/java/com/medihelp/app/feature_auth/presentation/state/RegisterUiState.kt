package com.medihelp.app.feature_auth.presentation.state

data class RegisterUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val registrationSucceeded: Boolean = false,
)
