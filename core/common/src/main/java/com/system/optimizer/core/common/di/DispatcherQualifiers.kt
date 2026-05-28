package com.system.optimizer.core.common.di

import javax.inject.Qualifier

/**
 * Qualifier for the IO-bounded [kotlinx.coroutines.CoroutineDispatcher]. Inject the
 * dispatcher rather than referencing [kotlinx.coroutines.Dispatchers.IO] directly so it
 * can be replaced with a [kotlinx.coroutines.test.TestDispatcher] in unit tests.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/**
 * Qualifier for the default (CPU-bounded) dispatcher.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

/**
 * Qualifier for the Main dispatcher (UI thread). Rare in business logic but handy for
 * test substitution.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher
