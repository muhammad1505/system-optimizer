package com.system.optimizer.core.domain.model

data class OptimizationResult(
    val type: String,
    val name: String,
    val description: String,
    val size: Long,
    val isOptimized: Boolean,
    val iconRes: Int
)

data class OptimizationSummary(
    val totalRamFreed: Long,
    val totalCacheCleared: Long,
    val processesKilled: Int,
    val batterySaved: Int,
    val junkFilesRemoved: Int,
    val totalOptimized: Long
)
