package com.medihelp.app.feature_medications.domain.model

import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MedicationSchedulingTest {

    private val today: LocalDate = LocalDate.of(2026, 7, 19)

    @Test
    fun `open ended course is active today`() {
        val medication = medication(startDate = null, endDate = null)

        assertTrue(medication.isActiveOn(today))
    }

    @Test
    fun `course is not active before it starts`() {
        val medication = medication(startDate = today.plusDays(1))

        assertFalse(medication.isActiveOn(today))
    }

    @Test
    fun `course is active on its first and last day`() {
        val medication = medication(startDate = today, endDate = today)

        assertTrue(medication.isActiveOn(today))
    }

    @Test
    fun `course is not active after it ends`() {
        val medication = medication(endDate = today.minusDays(1))

        assertFalse(medication.isActiveOn(today))
    }

    @Test
    fun `paused medication is never active`() {
        val medication = medication(status = MedicationStatus.PAUSED)

        assertFalse(medication.isActiveOn(today))
    }

    @Test
    fun `doses due counts every schedule of every active medication`() {
        val medications = listOf(
            medication(id = "a", scheduleCount = 3),
            medication(id = "b", scheduleCount = 1),
        )

        assertEquals(4, medications.dosesDueOn(today))
    }

    @Test
    fun `doses due ignores medications outside their date range`() {
        val medications = listOf(
            medication(id = "current", scheduleCount = 2),
            medication(id = "finished", scheduleCount = 5, endDate = today.minusDays(1)),
            medication(id = "future", scheduleCount = 5, startDate = today.plusDays(3)),
        )

        assertEquals(2, medications.dosesDueOn(today))
    }

    @Test
    fun `medication without schedules contributes no doses`() {
        val medications = listOf(medication(scheduleCount = 0))

        assertEquals(0, medications.dosesDueOn(today))
    }

    private fun medication(
        id: String = "id",
        status: MedicationStatus = MedicationStatus.ACTIVE,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        scheduleCount: Int = 1,
    ) = Medication(
        id = id,
        name = "Metformin",
        strength = "500 mg",
        dosageInstruction = "1 tablet after breakfast",
        simplifiedInstruction = null,
        purposeSimplified = null,
        startDate = startDate,
        endDate = endDate,
        status = status,
        requiresReview = false,
        isSynced = true,
        schedules = List(scheduleCount) { index ->
            MedicationSchedule(
                id = "$id-schedule-$index",
                timeOfDay = LocalTime.of(8 + index, 0),
                frequencyType = "daily",
                mealRelation = "after_meal",
                doseAmount = "1 tablet",
                notes = null,
            )
        },
    )
}
