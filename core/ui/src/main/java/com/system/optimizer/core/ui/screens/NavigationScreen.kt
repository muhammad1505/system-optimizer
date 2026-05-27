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
import com.system.optimizer.core.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    val description: String,
    val result: String
)

private val optimizationActions = listOf(
    OptimizationAction(
        key = "ram",
        title = "RAM Optimizer",
        description = "Analyze and free inactive memory",
        result = "Freed 512 MB RAM"
    ),
    OptimizationAction(
        key = "cache",
        title = "Cache Cleaner",
        description = "Remove temporary and junk cache",
        result = "Cleared 256 MB cache"
    ),
    OptimizationAction(
        key = "battery",
        title = "Battery Saver",
        description = "Tune background battery usage",
        result = "Estimated battery saving +20%"
    ),
    OptimizationAction(
        key = "process",
        title = "Process Manager",
        description = "Close unused background processes",
        result = "Closed 15 background processes"
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

    fun calculateProgress(): Float {
        val total = progressSteps.size
        if (total == 0) return 0f
        val completed = progressSteps.count { it.status == OptimizationStepStatus.COMPLETED }.toFloat()
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

    fun recordHistory(action: String, result: String) {
        val entry = HistoryEntry(
            action = action,
            result = result,
            timestamp = LocalDateTime.now().format(timeFormatter)
        )
        historyEntries = listOf(entry) + historyEntries
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
            delay(1000)
            markStepCompleted(action.key, action.result)
            progressMessage = "${action.title} completed"
            recordHistory(action = action.title, result = action.result)
            snackbarHostState.showSnackbar(action.result)
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

            optimizationActions.forEach { action ->
                progressMessage = "Running ${action.title}"
                markStepRunning(action.key)
                delay(900)
                markStepCompleted(action.key, action.result)
                recordHistory(action = action.title, result = action.result)
            }

            progressMessage = "Optimization completed successfully"
            snackbarHostState.showSnackbar("Optimization complete")
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
