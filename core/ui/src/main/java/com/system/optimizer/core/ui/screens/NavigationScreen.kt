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

enum class Screen(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    HISTORY("History", Icons.Default.History),
    SETTINGS("Settings", Icons.Default.Settings)
}

@Composable
fun NavigationScreen(modifier: Modifier = Modifier) {
    var selected by remember { mutableStateOf(Screen.HOME) }

    Scaffold(
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
        Box(modifier = Modifier.padding(padding)) {
            when (selected) {
                Screen.HOME -> HomeScreen(onOptimizeClick = {})
                Screen.HISTORY -> HistoryScreen()
                Screen.SETTINGS -> SettingsScreen(
                    onDarkModeChange = {},
                    onAutoOptimizeChange = {}
                )
            }
        }
    }
}
