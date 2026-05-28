package com.system.optimizer.di

import com.system.optimizer.core.data.repository.OptimizationRepositoryImpl
import com.system.optimizer.core.domain.repository.OptimizationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds repository interfaces to their default implementation.
 *
 * Both [com.system.optimizer.core.data.source.local.LocalDataSource] and
 * [OptimizationRepositoryImpl] are constructor-annotated with [javax.inject.Inject], so we
 * only need a [Binds] hookup to resolve the interface contract.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindOptimizationRepository(
        impl: OptimizationRepositoryImpl
    ): OptimizationRepository
}
