package com.system.optimizer.core.ui.screens

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.system.optimizer.core.ui.viewmodel.OptimizationViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private enum class Tab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    HISTORY("History", Icons.Default.History),
    SETTINGS("Settings", Icons.Default.Settings)
}

@Composable
fun NavigationScreen(
    modifier: Modifier = Modifier,
    viewModel: OptimizationViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val appContext = LocalContext.current.applicationContext
    val lifecycleOwner = LocalLifecycleOwner.current

    var selected by remember { mutableStateOf(Tab.HOME) }
    var usageAccessGranted by remember { mutableStateOf(false) }
    var batteryOptimizationIgnored by remember { mutableStateOf(false) }
    var notificationPermissionGranted by remember { mutableStateOf(false) }

    fun refreshAccessState() {
        usageAccessGranted = hasUsageAccess(appContext)
        batteryOptimizationIgnored = isBatteryOptimizationIgnored(appContext)
        notificationPermissionGranted = hasNotificationPermission(appContext)
    }

    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { refreshAccessState() }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { refreshAccessState() }

    // Observe lifecycle to re-check sensitive permission state when the user returns from
    // the system settings page.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshAccessState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // One-time initial permission probe.
    LaunchedEffect(Unit) {
        refreshAccessState()
    }

    // Forward ViewModel one-shot events to the snackbar host.
    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is OptimizationViewModel.UiEvent.Snackbar -> {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    fun openSettingsPage(intent: Intent, unavailableMessage: String) {
        runCatching {
            settingsLauncher.launch(intent)
        }.onFailure {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(unavailableMessage)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        selected = selected == tab,
                        onClick = { selected = tab }
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
                Tab.HOME -> HomeScreen(
                    state = state,
                    onRunSingle = viewModel::runSingleAction,
                    onRunAll = viewModel::runOptimizeAll
                )
                Tab.HISTORY -> HistoryScreen(
                    history = state.history,
                    onClearHistory = viewModel::clearHistory
                )
                Tab.SETTINGS -> SettingsScreen(
                    darkModeEnabled = state.isDarkMode,
                    autoOptimizeEnabled = state.isAutoOptimize,
                    usageAccessGranted = usageAccessGranted,
                    batteryOptimizationIgnored = batteryOptimizationIgnored,
                    notificationPermissionGranted = notificationPermissionGranted,
                    isBusy = state.isOptimizing,
                    totalOptimized = state.totalOptimized,
                    onDarkModeChange = viewModel::setDarkMode,
                    onAutoOptimizeChange = viewModel::setAutoOptimize,
                    onOpenUsageAccessSettings = {
                        openSettingsPage(
                            intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
                            unavailableMessage = "Usage access settings unavailable"
                        )
                    },
                    onOpenBatteryOptimizationSettings = {
                        openSettingsPage(
                            intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS),
                            unavailableMessage = "Battery optimization settings unavailable"
                        )
                    },
                    onRequestNotificationPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            !notificationPermissionGranted
                        ) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            openSettingsPage(
                                intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, appContext.packageName)
                                },
                                unavailableMessage = "Notification settings unavailable"
                            )
                        }
                    }
                )
            }
        }
    }
}

private fun hasUsageAccess(context: Context): Boolean {
    val appOpsManager = context.getSystemService(AppOpsManager::class.java) ?: return false
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOpsManager.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
    } else {
        @Suppress("DEPRECATION")
        appOpsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
    }
    return mode == AppOpsManager.MODE_ALLOWED
}

private fun isBatteryOptimizationIgnored(context: Context): Boolean {
    val powerManager = context.getSystemService(PowerManager::class.java) ?: return false
    return powerManager.isIgnoringBatteryOptimizations(context.packageName)
}

private fun hasNotificationPermission(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED
}
