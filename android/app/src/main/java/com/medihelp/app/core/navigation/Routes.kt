package com.medihelp.app.core.navigation

object Routes {
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DASHBOARD = "dashboard"
    const val MEDICATIONS = "medications"
    const val ADD_MEDICATION = "add_medication"
    const val MEDICATION_DETAIL = "medication_detail/{medicationId}"

    fun medicationDetail(medicationId: String) = "medication_detail/$medicationId"
}
