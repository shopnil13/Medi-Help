package com.medihelp.app.feature_dashboard.domain

import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class DayGreetingTest {

    @Test
    fun `morning starts at five`() {
        assertEquals(DayGreeting.MORNING, DayGreeting.forTime(LocalTime.of(5, 0)))
        assertEquals(DayGreeting.MORNING, DayGreeting.forTime(LocalTime.of(11, 59)))
    }

    @Test
    fun `afternoon starts at noon`() {
        assertEquals(DayGreeting.AFTERNOON, DayGreeting.forTime(LocalTime.of(12, 0)))
        assertEquals(DayGreeting.AFTERNOON, DayGreeting.forTime(LocalTime.of(16, 59)))
    }

    @Test
    fun `evening starts at five in the afternoon`() {
        assertEquals(DayGreeting.EVENING, DayGreeting.forTime(LocalTime.of(17, 0)))
        assertEquals(DayGreeting.EVENING, DayGreeting.forTime(LocalTime.of(23, 59)))
    }

    @Test
    fun `after midnight still reads as evening`() {
        assertEquals(DayGreeting.EVENING, DayGreeting.forTime(LocalTime.of(0, 0)))
        assertEquals(DayGreeting.EVENING, DayGreeting.forTime(LocalTime.of(4, 59)))
    }
}
