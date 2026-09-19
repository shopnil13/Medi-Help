package com.medihelp.app.feature_medications.domain.model

import java.time.LocalDate

/**
 * Whether this medication is meant to be taken on [date].
 *
 * A null start or end date means the course is open-ended on that side, which
 * is how manually added medicines without an explicit range are stored.
 */
fun Medication.isActiveOn(date: LocalDate): Boolean {
    if (status != MedicationStatus.ACTIVE) return false
    if (startDate != null && date.isBefore(startDate)) return false
    if (endDate != null && date.isAfter(endDate)) return false
    return true
}

/**
 * Number of individual doses scheduled across all medications on [date].
 *
 * The dashboard reports doses rather than medicines: a medicine taken three
 * times a day is three things the user still has to do today.
 */
fun List<Medication>.dosesDueOn(date: LocalDate): Int =
    filter { it.isActiveOn(date) }.sumOf { it.schedules.size }
