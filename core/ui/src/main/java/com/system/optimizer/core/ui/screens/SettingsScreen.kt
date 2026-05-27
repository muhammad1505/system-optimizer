package com.system.optimizer.core.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    darkModeEnabled: Boolean,
    autoOptimizeEnabled: Boolean,
    isBusy: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onAutoOptimizeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text("Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text("Dark Mode"); Text("Better for battery", style = MaterialTheme.typography.bodySmall) }
                Switch(
                    checked = darkModeEnabled,
                    enabled = !isBusy,
                    onCheckedChange = onDarkModeChange
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text("Auto Optimize"); Text("When battery low", style = MaterialTheme.typography.bodySmall) }
                Switch(
                    checked = autoOptimizeEnabled,
                    enabled = !isBusy,
                    onCheckedChange = onAutoOptimizeChange
                )
            }
            Spacer(Modifier.height(32.dp))
            Text("System Optimizer v1.0.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
        }
    }
}
