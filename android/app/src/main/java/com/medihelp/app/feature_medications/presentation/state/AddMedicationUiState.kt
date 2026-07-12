package com.medihelp.app.feature_medications.presentation.state

import java.time.LocalTime

data class AddMedicationUiState(
    val name: String = "",
    val strength: String = "",
    val dosageInstruction: String = "",
    val doseAmount: String = "",
    val timeOfDay: LocalTime = LocalTime.of(8, 0),
    val mealRelation: String = "unknown",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val saveSucceeded: Boolean = false,
)

val MEAL_RELATION_OPTIONS: List<Pair<String, String>> = listOf(
    "before_food" to "Before food",
    "after_food" to "After food",
    "with_food" to "With food",
    "no_relation" to "Anytime",
)
