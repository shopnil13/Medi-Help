package com.medihelp.app.feature_vitals.presentation.state

import java.time.Instant

enum class AddVitalKind(val label: String) {
    BLOOD_PRESSURE("Blood pressure"),
    HEART_RATE("Heart rate"),
    BLOOD_GLUCOSE("Glucose"),
    WEIGHT("Weight"),
    CUSTOM("Custom"),
}

data class AddVitalUiState(
    val kind: AddVitalKind = AddVitalKind.BLOOD_PRESSURE,
    val primaryValue: String = "",
    val secondaryValue: String = "",
    val customName: String = "",
    val unit: String = "mmHg",
    val notes: String = "",
    val recordedAt: Instant = Instant.now(),
    val isSaving: Boolean = false,
    val saveSucceeded: Boolean = false,
    val errorMessage: String? = null,
)

