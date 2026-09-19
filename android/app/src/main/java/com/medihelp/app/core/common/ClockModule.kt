package com.medihelp.app.core.common

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

/**
 * Supplies the app's clock.
 *
 * Injecting [Clock] instead of calling `LocalDate.now()` directly lets date and
 * time boundaries — "due today", the time-of-day greeting — be tested at a
 * fixed instant rather than only when the suite happens to run.
 */
@Module
@InstallIn(SingletonComponent::class)
object ClockModule {

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemDefaultZone()
}
