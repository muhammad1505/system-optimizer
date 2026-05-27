package com.system.optimizer.core.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import com.system.optimizer.core.common.Result
import com.system.optimizer.core.data.repository.OptimizationRepositoryImpl
import com.system.optimizer.core.data.source.local.LocalDataSource
import com.system.optimizer.core.domain.usecase.ClearCacheUseCase
import com.system.optimizer.core.domain.usecase.KillProcessesUseCase
import com.system.optimizer.core.domain.usecase.OptimizeBatteryUseCase
import com.system.optimizer.core.domain.usecase.OptimizeRamUseCase
import com.system.optimizer.core.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class Screen(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    HISTORY("History", Icons.Default.History),
    SETTINGS("Settings", Icons.Default.Settings)
}

private data class OptimizationAction(
    val key: String,
    val title: String,
    val description: String
)

private val optimizationActions = listOf(
    OptimizationAction(
        key = "ram",
        title = "RAM Optimizer",
        description = "Analyze and free inactive memory"
    ),
    OptimizationAction(
        key = "cache",
        title = "Cache Cleaner",
        description = "Remove temporary and junk cache"
    ),
    OptimizationAction(
        key = "battery",
        title = "Battery Saver",
        description = "Tune background battery usage"
    ),
    OptimizationAction(
        key = "process",
        title = "Process Manager",
        description = "Close unused background processes"
    )
)

private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

private fun buildInitialProgressSteps(): List<OptimizationStepUi> =
    optimizationActions.map { action ->
        OptimizationStepUi(
            key = action.key,
            title = action.title,
            description = action.description
        )
    }

@Composable
fun NavigationScreen(modifier: Modifier = Modifier) {
    var selected by remember { mutableStateOf(Screen.HOME) }
    var isOptimizing by remember { mutableStateOf(false) }
    var isDarkModeEnabled by remember { mutableStateOf(false) }
    var isAutoOptimizeEnabled by remember { mutableStateOf(false) }
    var historyEntries by remember { mutableStateOf(emptyList<HistoryEntry>()) }
    var progressMessage by remember { mutableStateOf("Ready to run optimization") }
    var progressSteps by remember { mutableStateOf(buildInitialProgressSteps()) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val appContext = LocalContext.current.applicationContext

    val repository = remember(appContext) {
        OptimizationRepositoryImpl(
            localDataSource = LocalDataSource(appContext),
            appContext = appContext
        )
    }
    val optimizeRamUseCase = remember(repository) { OptimizeRamUseCase(repository) }
    val clearCacheUseCase = remember(repository) { ClearCacheUseCase(repository) }
    val optimizeBatteryUseCase = remember(repository) { OptimizeBatteryUseCase(repository) }
    val killProcessesUseCase = remember(repository) { KillProcessesUseCase(repository) }

    fun bytesToReadable(bytes: Long): String {
        if (bytes <= 0L) return "0 B"
        val units = listOf("B", "KB", "MB", "GB", "TB")
        var value = bytes.toDouble()
        var idx = 0
        while (value >= 1024 && idx < units.lastIndex) {
            value /= 1024
            idx += 1
        }
        val shown = if (idx == 0) value.toInt().toString() else String.format("%.1f", value)
        return "$shown ${units[idx]}"
    }

    fun calculateProgress(): Float {
        val total = progressSteps.size
        if (total == 0) return 0f
        val completed = progressSteps.count {
            it.status == OptimizationStepStatus.COMPLETED || it.status == OptimizationStepStatus.FAILED
        }.toFloat()
        val runningBonus = if (progressSteps.any { it.status == OptimizationStepStatus.RUNNING }) 0.4f else 0f
        return ((completed + runningBonus) / total.toFloat()).coerceIn(0f, 1f)
    }

    fun resetProgressState() {
        progressSteps = buildInitialProgressSteps()
        progressMessage = "Preparing optimization tasks"
    }

    fun markStepRunning(key: String) {
        progressSteps = progressSteps.map { step ->
            when {
                step.key == key -> step.copy(status = OptimizationStepStatus.RUNNING)
                step.status == OptimizationStepStatus.RUNNING -> step.copy(status = OptimizationStepStatus.PENDING)
                else -> step
            }
        }
    }

    fun markStepCompleted(key: String, result: String) {
        progressSteps = progressSteps.map { step ->
            if (step.key == key) {
                step.copy(status = OptimizationStepStatus.COMPLETED, result = result)
            } else {
                step
            }
        }
    }

    fun markStepFailed(key: String, message: String) {
        progressSteps = progressSteps.map { step ->
            if (step.key == key) {
                step.copy(status = OptimizationStepStatus.FAILED, result = message)
            } else {
                step
            }
        }
    }

    fun recordHistory(action: String, result: String) {
        val entry = HistoryEntry(
            action = action,
            result = result,
            timestamp = LocalDateTime.now().format(timeFormatter)
        )
        historyEntries = listOf(entry) + historyEntries
    }

    suspend fun executeAction(action: OptimizationAction): Pair<Boolean, String> {
        return when (action.key) {
            "ram" -> {
                when (val result = withContext(Dispatchers.IO) { optimizeRamUseCase() }) {
                    is Result.Success -> {
                        val detail = if (result.data > 0L) {
                            "Freed ${bytesToReadable(result.data)} RAM"
                        } else {
                            "No significant RAM reclaimed"
                        }
                        true to detail
                    }
                    is Result.Error -> false to "RAM optimization failed: ${result.exception.message ?: "unknown error"}"
                    Result.Loading -> false to "RAM optimization still loading"
                }
            }

            "cache" -> {
                when (val result = withContext(Dispatchers.IO) { clearCacheUseCase() }) {
                    is Result.Success -> {
                        val detail = if (result.data > 0L) {
                            "Cleared ${bytesToReadable(result.data)} cache"
                        } else {
                            "No cache files to clear"
                        }
                        true to detail
                    }
                    is Result.Error -> false to "Cache clean failed: ${result.exception.message ?: "unknown error"}"
                    Result.Loading -> false to "Cache cleaning still loading"
                }
            }

            "battery" -> {
                when (val result = withContext(Dispatchers.IO) { optimizeBatteryUseCase() }) {
                    is Result.Success -> {
                        val detail = if (result.data > 0) {
                            "Estimated battery saving +${result.data}%"
                        } else {
                            "No additional battery saving detected"
                        }
                        true to detail
                    }
                    is Result.Error -> false to "Battery optimization failed: ${result.exception.message ?: "unknown error"}"
                    Result.Loading -> false to "Battery optimization still loading"
                }
            }

            "process" -> {
                when (val result = withContext(Dispatchers.IO) { killProcessesUseCase() }) {
                    is Result.Success -> {
                        val detail = if (result.data > 0) {
                            "Requested stop for ${result.data} background app(s)"
                        } else {
                            "No eligible background app to stop"
                        }
                        true to detail
                    }
                    is Result.Error -> false to "Process cleanup failed: ${result.exception.message ?: "unknown error"}"
                    Result.Loading -> false to "Process cleanup still loading"
                }
            }

            else -> false to "Unknown optimization action"
        }
    }

    fun runSingleAction(action: OptimizationAction) {
        if (isOptimizing) return
        coroutineScope.launch {
            isOptimizing = true
            resetProgressState()
            snackbarHostState.currentSnackbarData?.dismiss()
            progressMessage = "Running ${action.title}"
            markStepRunning(action.key)
            snackbarHostState.showSnackbar("Starting ${action.title}...")
            delay(300)

            val (ok, detail) = executeAction(action)
            if (ok) {
                markStepCompleted(action.key, detail)
                progressMessage = "${action.title} completed"
            } else {
                markStepFailed(action.key, detail)
                progressMessage = "${action.title} failed"
            }

            recordHistory(action = action.title, result = detail)
            snackbarHostState.showSnackbar(detail)
            isOptimizing = false
        }
    }

    fun runOptimizeAll() {
        if (isOptimizing) return
        coroutineScope.launch {
            isOptimizing = true
            resetProgressState()
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar("Starting full optimization...")

            var failCount = 0
            optimizationActions.forEach { action ->
                progressMessage = "Running ${action.title}"
                markStepRunning(action.key)
                delay(250)

                val (ok, detail) = executeAction(action)
                if (ok) {
                    markStepCompleted(action.key, detail)
                } else {
                    markStepFailed(action.key, detail)
                    failCount += 1
                }
                recordHistory(action = action.title, result = detail)
            }

            progressMessage = if (failCount == 0) {
                "Optimization completed successfully"
            } else {
                "Optimization finished with $failCount issue(s)"
            }
            snackbarHostState.showSnackbar(progressMessage)
            isOptimizing = false
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                Screen.entries.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = selected == screen,
                        onClick = { selected = screen }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selected) {
                Screen.HOME -> HomeScreen(
                    isOptimizing = isOptimizing,
                    progressMessage = progressMessage,
                    progressPercent = calculateProgress(),
                    progressSteps = progressSteps,
                    onOptimizeRamClick = {
                        runSingleAction(optimizationActions.first { it.key == "ram" })
                    },
                    onClearCacheClick = {
                        runSingleAction(optimizationActions.first { it.key == "cache" })
                    },
                    onOptimizeBatteryClick = {
                        runSingleAction(optimizationActions.first { it.key == "battery" })
                    },
                    onKillProcessesClick = {
                        runSingleAction(optimizationActions.first { it.key == "process" })
                    },
                    onOptimizeAllClick = { runOptimizeAll() }
                )
                Screen.HISTORY -> HistoryScreen(history = historyEntries)
                Screen.SETTINGS -> SettingsScreen(
                    darkModeEnabled = isDarkModeEnabled,
                    autoOptimizeEnabled = isAutoOptimizeEnabled,
                    isBusy = isOptimizing,
                    onDarkModeChange = { enabled ->
                        isDarkModeEnabled = enabled
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                if (enabled) "Dark mode enabled" else "Dark mode disabled"
                            )
                        }
                    },
                    onAutoOptimizeChange = { enabled ->
                        isAutoOptimizeEnabled = enabled
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                if (enabled) "Auto optimize enabled" else "Auto optimize disabled"
                            )
                        }
                    }
                )
            }
        }
    }
}
