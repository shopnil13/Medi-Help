package com.medihelp.app.feature_vitals.di

import com.medihelp.app.feature_vitals.data.remote.VitalApi
import com.medihelp.app.feature_vitals.data.repository.VitalRepositoryImpl
import com.medihelp.app.feature_vitals.domain.repository.VitalRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit
import retrofit2.create

@Module
@InstallIn(SingletonComponent::class)
object VitalApiModule {
    @Provides
    @Singleton
    fun provideVitalApi(retrofit: Retrofit): VitalApi = retrofit.create()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class VitalRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindVitalRepository(impl: VitalRepositoryImpl): VitalRepository
}

