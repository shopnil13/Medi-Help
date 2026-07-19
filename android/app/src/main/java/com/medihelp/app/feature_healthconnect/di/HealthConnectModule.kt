package com.medihelp.app.feature_healthconnect.di

import com.medihelp.app.feature_healthconnect.data.HealthConnectRepositoryImpl
import com.medihelp.app.feature_healthconnect.domain.repository.HealthConnectRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HealthConnectModule {
    @Binds
    @Singleton
    abstract fun bindHealthConnectRepository(
        implementation: HealthConnectRepositoryImpl,
    ): HealthConnectRepository
}
