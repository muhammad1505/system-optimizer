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

private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

@Composable
fun NavigationScreen(modifier: Modifier = Modifier) {
    var selected by remember { mutableStateOf(Screen.HOME) }
    var isOptimizing by remember { mutableStateOf(false) }
    var isDarkModeEnabled by remember { mutableStateOf(false) }
    var isAutoOptimizeEnabled by remember { mutableStateOf(false) }
    var historyEntries by remember { mutableStateOf(emptyList<HistoryEntry>()) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    fun recordHistory(action: String, result: String) {
        val entry = HistoryEntry(
            action = action,
            result = result,
            timestamp = LocalDateTime.now().format(timeFormatter)
        )
        historyEntries = listOf(entry) + historyEntries
    }

    fun runSingleAction(action: String, result: String) {
        if (isOptimizing) return
        coroutineScope.launch {
            isOptimizing = true
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar("Running $action...")
            delay(700)
            recordHistory(action = action, result = result)
            snackbarHostState.showSnackbar(result)
            isOptimizing = false
        }
    }

    fun runOptimizeAll() {
        if (isOptimizing) return
        coroutineScope.launch {
            isOptimizing = true
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar("Running full optimization...")

            val steps = listOf(
                "RAM Optimizer" to "Freed 512 MB RAM",
                "Cache Cleaner" to "Cleared 256 MB cache",
                "Battery Saver" to "Estimated battery saving +20%",
                "Process Manager" to "Closed 15 background processes"
            )
            steps.forEach { (action, result) ->
                delay(450)
                recordHistory(action = action, result = result)
            }

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
                    onOptimizeRamClick = {
                        runSingleAction(
                            action = "RAM Optimizer",
                            result = "Freed 512 MB RAM"
                        )
                    },
                    onClearCacheClick = {
                        runSingleAction(
                            action = "Cache Cleaner",
                            result = "Cleared 256 MB cache"
                        )
                    },
                    onOptimizeBatteryClick = {
                        runSingleAction(
                            action = "Battery Saver",
                            result = "Estimated battery saving +20%"
                        )
                    },
                    onKillProcessesClick = {
                        runSingleAction(
                            action = "Process Manager",
                            result = "Closed 15 background processes"
                        )
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
