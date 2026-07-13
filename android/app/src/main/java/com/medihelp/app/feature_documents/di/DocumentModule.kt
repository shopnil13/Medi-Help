package com.medihelp.app.feature_documents.di

import com.medihelp.app.feature_documents.data.remote.DocumentApi
import com.medihelp.app.feature_documents.data.repository.DocumentRepositoryImpl
import com.medihelp.app.feature_documents.domain.repository.DocumentRepository
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
object DocumentApiModule {
    @Provides
    @Singleton
    fun provideDocumentApi(retrofit: Retrofit): DocumentApi = retrofit.create()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DocumentRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindDocumentRepository(impl: DocumentRepositoryImpl): DocumentRepository
}
