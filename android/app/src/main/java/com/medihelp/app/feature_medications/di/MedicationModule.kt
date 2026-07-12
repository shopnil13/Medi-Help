package com.medihelp.app.feature_medications.di

import com.medihelp.app.feature_medications.data.remote.MedicationApi
import com.medihelp.app.feature_medications.data.repository.MedicationRepositoryImpl
import com.medihelp.app.feature_medications.domain.repository.MedicationRepository
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
object MedicationApiModule {
    @Provides
    @Singleton
    fun provideMedicationApi(retrofit: Retrofit): MedicationApi = retrofit.create()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class MedicationRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindMedicationRepository(impl: MedicationRepositoryImpl): MedicationRepository
}
