package com.system.optimizer.core.ui.model

/**
 * Lifecycle status of a single optimization step within the UI.
 */
enum class OptimizationStepStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED
}

/**
 * Pure UI state of an optimization step shown in the home/timeline.
 */
data class OptimizationStepUi(
    val key: String,
    val title: String,
    val description: String,
    val status: OptimizationStepStatus = OptimizationStepStatus.PENDING,
    val result: String = ""
)

/**
 * History row displayed in the History tab.
 */
data class HistoryEntry(
    val action: String,
    val result: String,
    val timestamp: String,
    val isFailure: Boolean = false
)

/**
 * Definition of an optimization action exposed to the UI.
 */
data class OptimizationAction(
    val key: String,
    val title: String,
    val description: String
)

/**
 * Stable definition of supported optimization actions.
 */
val DEFAULT_OPTIMIZATION_ACTIONS: List<OptimizationAction> = listOf(
    OptimizationAction(
        key = "ram",
        title = "RAM Optimizer",
        description = "Free inactive memory and reduce pressure"
    ),
    OptimizationAction(
        key = "cache",
        title = "Cache Cleaner",
        description = "Remove temporary and residual files"
    ),
    OptimizationAction(
        key = "battery",
        title = "Battery Saver",
        description = "Tune background battery consumption"
    ),
    OptimizationAction(
        key = "process",
        title = "Process Manager",
        description = "Stop unused background app processes"
    )
)
