package com.system.optimizer.di

import android.content.Context
import com.system.optimizer.core.data.repository.OptimizationRepositoryImpl
import com.system.optimizer.core.data.source.local.LocalDataSource
import com.system.optimizer.core.domain.repository.OptimizationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideLocalDataSource(@ApplicationContext context: Context): LocalDataSource {
        return LocalDataSource(context)
    }

    @Provides
    @Singleton
    fun provideOptimizationRepository(localDataSource: LocalDataSource): OptimizationRepository {
        return OptimizationRepositoryImpl(localDataSource)
    }
}
