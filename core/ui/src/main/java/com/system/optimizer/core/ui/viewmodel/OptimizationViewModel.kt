package com.system.optimizer.core.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.system.optimizer.core.common.Constants
import com.system.optimizer.core.common.BytesFormatter
import com.system.optimizer.core.common.Result
import com.system.optimizer.core.data.source.local.LocalDataSource
import com.system.optimizer.core.domain.usecase.ClearCacheUseCase
import com.system.optimizer.core.domain.usecase.KillProcessesUseCase
import com.system.optimizer.core.domain.usecase.OptimizeBatteryUseCase
import com.system.optimizer.core.domain.usecase.OptimizeRamUseCase
import com.system.optimizer.core.ui.model.DEFAULT_OPTIMIZATION_ACTIONS
import com.system.optimizer.core.ui.model.HistoryEntry
import com.system.optimizer.core.ui.model.HistorySerializer
import com.system.optimizer.core.ui.model.OptimizationAction
import com.system.optimizer.core.ui.model.OptimizationStepStatus
import com.system.optimizer.core.ui.model.OptimizationStepUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Aggregated UI state for the home, history and settings tabs.
 *
 * This is intentionally a single state object so the various screens can derive what they
 * need with [androidx.compose.runtime.collectAsState] without needing extra view-model
 * subclasses.
 */
data class OptimizationUiState(
    val isOptimizing: Boolean = false,
    val isDarkMode: Boolean = false,
    val isAutoOptimize: Boolean = false,
    val progressMessage: String = "Ready to run optimization",
    val progressSteps: List<OptimizationStepUi> = DEFAULT_OPTIMIZATION_ACTIONS.map { action ->
        OptimizationStepUi(
            key = action.key,
            title = action.title,
            description = action.description
        )
    },
    val history: List<HistoryEntry> = emptyList(),
    val totalOptimized: Int = 0,
    val lastOptimizeMillis: Long = 0L
) {
    val totalSteps: Int get() = progressSteps.size
    val completedSteps: Int
        get() = progressSteps.count {
            it.status == OptimizationStepStatus.COMPLETED ||
                it.status == OptimizationStepStatus.FAILED
        }
    val runningSteps: Int
        get() = progressSteps.count { it.status == OptimizationStepStatus.RUNNING }
    val failedSteps: Int
        get() = progressSteps.count { it.status == OptimizationStepStatus.FAILED }

    val progressPercent: Float
        get() {
            if (totalSteps == 0) return 0f
            val running = if (runningSteps > 0) 0.4f else 0f
            return ((completedSteps + running) / totalSteps.toFloat()).coerceIn(0f, 1f)
        }
}

@HiltViewModel
class OptimizationViewModel @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val optimizeRamUseCase: OptimizeRamUseCase,
    private val clearCacheUseCase: ClearCacheUseCase,
    private val optimizeBatteryUseCase: OptimizeBatteryUseCase,
    private val killProcessesUseCase: KillProcessesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OptimizationUiState())
    val uiState: StateFlow<OptimizationUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    private val timeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    init {
        // Hydrate from prefs
        _uiState.update { state ->
            state.copy(
                isDarkMode = localDataSource.isDarkMode,
                isAutoOptimize = localDataSource.isAutoOptimize,
                history = HistorySerializer.decode(localDataSource.historyJson),
                totalOptimized = localDataSource.totalOptimized,
                lastOptimizeMillis = localDataSource.lastOptimize
            )
        }
    }

    fun setDarkMode(enabled: Boolean) {
        if (_uiState.value.isDarkMode == enabled) return
        localDataSource.isDarkMode = enabled
        _uiState.update { it.copy(isDarkMode = enabled) }
        emit(UiEvent.Snackbar(if (enabled) "Dark mode enabled" else "Dark mode disabled"))
    }

    fun setAutoOptimize(enabled: Boolean) {
        if (_uiState.value.isAutoOptimize == enabled) return
        localDataSource.isAutoOptimize = enabled
        _uiState.update { it.copy(isAutoOptimize = enabled) }
        emit(UiEvent.Snackbar(if (enabled) "Auto optimize enabled" else "Auto optimize disabled"))
    }

    fun clearHistory() {
        localDataSource.clearHistory()
        _uiState.update { it.copy(history = emptyList()) }
        emit(UiEvent.Snackbar("History cleared"))
    }

    /** Run a single module by [key]. */
    fun runSingleAction(key: String) {
        val action = DEFAULT_OPTIMIZATION_ACTIONS.firstOrNull { it.key == key } ?: return
        if (_uiState.value.isOptimizing) return
        viewModelScope.launch {
            startOptimization()
            _uiState.update { state ->
                state.copy(
                    progressMessage = "Running ${action.title}",
                    progressSteps = state.progressSteps.map { step ->
                        when (step.key) {
                            action.key -> step.copy(status = OptimizationStepStatus.RUNNING, result = "")
                            else -> step.copy(status = OptimizationStepStatus.PENDING, result = "")
                        }
                    }
                )
            }
            emit(UiEvent.Snackbar("Starting ${action.title}…"))
            delay(200)

            val (ok, detail) = executeAction(action)
            applyStepOutcome(action.key, ok, detail)
            recordHistory(action.title, detail, !ok)
            _uiState.update { state ->
                state.copy(
                    progressMessage = if (ok) "${action.title} completed"
                    else "${action.title} failed"
                )
            }
            emit(UiEvent.Snackbar(detail))
            finishOptimization(success = ok)
        }
    }

    /** Run all modules sequentially. */
    fun runOptimizeAll() {
        if (_uiState.value.isOptimizing) return
        viewModelScope.launch {
            startOptimization()
            emit(UiEvent.Snackbar("Starting full optimization…"))

            var failureCount = 0
            DEFAULT_OPTIMIZATION_ACTIONS.forEach { action ->
                _uiState.update { state ->
                    state.copy(
                        progressMessage = "Running ${action.title}",
                        progressSteps = state.progressSteps.map { step ->
                            if (step.key == action.key) step.copy(status = OptimizationStepStatus.RUNNING)
                            else step
                        }
                    )
                }
                delay(200)
                val (ok, detail) = executeAction(action)
                if (!ok) failureCount += 1
                applyStepOutcome(action.key, ok, detail)
                recordHistory(action.title, detail, !ok)
            }

            val finalMessage = if (failureCount == 0) {
                "Full optimization completed successfully"
            } else {
                "Optimization finished with $failureCount issue(s)"
            }
            _uiState.update { it.copy(progressMessage = finalMessage) }
            emit(UiEvent.Snackbar(finalMessage))
            finishOptimization(success = failureCount == 0)
        }
    }

    private fun startOptimization() {
        _uiState.update { state ->
            state.copy(
                isOptimizing = true,
                progressSteps = DEFAULT_OPTIMIZATION_ACTIONS.map { action ->
                    OptimizationStepUi(
                        key = action.key,
                        title = action.title,
                        description = action.description
                    )
                },
                progressMessage = "Preparing optimization tasks"
            )
        }
    }

    private fun finishOptimization(success: Boolean) {
        if (success) {
            localDataSource.lastOptimize = System.currentTimeMillis()
            localDataSource.totalOptimized = localDataSource.totalOptimized + 1
        }
        _uiState.update { state ->
            state.copy(
                isOptimizing = false,
                totalOptimized = localDataSource.totalOptimized,
                lastOptimizeMillis = localDataSource.lastOptimize
            )
        }
    }

    private fun applyStepOutcome(key: String, ok: Boolean, detail: String) {
        _uiState.update { state ->
            state.copy(
                progressSteps = state.progressSteps.map { step ->
                    if (step.key == key) {
                        step.copy(
                            status = if (ok) OptimizationStepStatus.COMPLETED
                            else OptimizationStepStatus.FAILED,
                            result = detail
                        )
                    } else step
                }
            )
        }
    }

    private fun recordHistory(action: String, detail: String, isFailure: Boolean) {
        val entry = HistoryEntry(
            action = action,
            result = detail,
            timestamp = LocalDateTime.now().format(timeFormatter),
            isFailure = isFailure
        )
        val updated = (listOf(entry) + _uiState.value.history)
            .take(Constants.MAX_HISTORY_ENTRIES)
        _uiState.update { it.copy(history = updated) }
        localDataSource.historyJson = HistorySerializer.encode(updated)
    }

    private suspend fun executeAction(action: OptimizationAction): Pair<Boolean, String> {
        return when (action.key) {
            "ram" -> when (val result = withContext(Dispatchers.IO) { optimizeRamUseCase() }) {
                is Result.Success -> {
                    val detail = if (result.data > 0L) "Freed ${BytesFormatter.toReadable(result.data)} RAM"
                    else "No significant RAM reclaimed"
                    true to detail
                }
                is Result.Error -> false to errorDetail("RAM optimization", result.exception)
                Result.Loading -> false to "RAM optimization still loading"
            }

            "cache" -> when (val result = withContext(Dispatchers.IO) { clearCacheUseCase() }) {
                is Result.Success -> {
                    val detail = if (result.data > 0L) "Cleared ${BytesFormatter.toReadable(result.data)} cache"
                    else "No cache files to clear"
                    true to detail
                }
                is Result.Error -> false to errorDetail("Cache cleanup", result.exception)
                Result.Loading -> false to "Cache cleaning still loading"
            }

            "battery" -> when (val result = withContext(Dispatchers.IO) { optimizeBatteryUseCase() }) {
                is Result.Success -> {
                    val detail = if (result.data > 0) "Estimated battery saving +${result.data}%"
                    else "No additional battery saving detected"
                    true to detail
                }
                is Result.Error -> false to errorDetail("Battery optimization", result.exception)
                Result.Loading -> false to "Battery optimization still loading"
            }

            "process" -> when (val result = withContext(Dispatchers.IO) { killProcessesUseCase() }) {
                is Result.Success -> {
                    val detail = if (result.data > 0) "Requested stop for ${result.data} background app(s)"
                    else "No eligible background app to stop"
                    true to detail
                }
                is Result.Error -> false to errorDetail("Process cleanup", result.exception)
                Result.Loading -> false to "Process cleanup still loading"
            }

            else -> false to "Unknown optimization action"
        }
    }

    private fun errorDetail(label: String, throwable: Throwable): String {
        val msg = throwable.message?.takeIf { it.isNotBlank() } ?: "unknown error"
        return "$label failed: $msg"
    }

    private fun emit(event: UiEvent) {
        _events.tryEmit(event)
    }

    /** One-shot UI events the screen can consume (snackbars, navigation). */
    sealed interface UiEvent {
        data class Snackbar(val message: String) : UiEvent
    }
}
