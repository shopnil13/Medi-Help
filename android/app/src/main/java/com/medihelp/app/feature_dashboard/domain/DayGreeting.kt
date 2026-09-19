package com.medihelp.app.feature_dashboard.domain

import java.time.LocalTime

/** Time-of-day greeting shown at the top of the dashboard. */
enum class DayGreeting {
    MORNING,
    AFTERNOON,
    EVENING,
    ;

    companion object {
        private val MORNING_START: LocalTime = LocalTime.of(5, 0)
        private val AFTERNOON_START: LocalTime = LocalTime.of(12, 0)
        private val EVENING_START: LocalTime = LocalTime.of(17, 0)

        /** Late night (before 05:00) reads as evening rather than morning. */
        fun forTime(time: LocalTime): DayGreeting = when {
            time < MORNING_START -> EVENING
            time < AFTERNOON_START -> MORNING
            time < EVENING_START -> AFTERNOON
            else -> EVENING
        }
    }
}
