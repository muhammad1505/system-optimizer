package com.system.optimizer.core.domain.repository

import com.system.optimizer.core.common.Result
import com.system.optimizer.core.domain.model.OptimizationResult
import com.system.optimizer.core.domain.model.OptimizationSummary
import kotlinx.coroutines.flow.Flow

interface OptimizationRepository {
    fun getRamInfo(): Flow<Result<List<OptimizationResult>>>
    fun getCacheInfo(): Flow<Result<List<OptimizationResult>>>
    fun getBatteryInfo(): Flow<Result<List<OptimizationResult>>>
    fun getRunningProcesses(): Flow<Result<List<OptimizationResult>>>
    
    suspend fun optimizeRam(): Result<Long>
    suspend fun clearCache(): Result<Long>
    suspend fun killBackgroundProcesses(): Result<Int>
    suspend fun optimizeBattery(): Result<Int>
    
    fun getOptimizationHistory(): Flow<List<OptimizationSummary>>
    suspend fun saveOptimizationSummary(summary: OptimizationSummary)
}
