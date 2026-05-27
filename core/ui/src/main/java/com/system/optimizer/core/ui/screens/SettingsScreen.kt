package com.system.optimizer.core.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    darkModeEnabled: Boolean,
    autoOptimizeEnabled: Boolean,
    usageAccessGranted: Boolean,
    batteryOptimizationIgnored: Boolean,
    notificationPermissionGranted: Boolean,
    isBusy: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onAutoOptimizeChange: (Boolean) -> Unit,
    onOpenUsageAccessSettings: () -> Unit,
    onOpenBatteryOptimizationSettings: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
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
            Spacer(Modifier.height(24.dp))

            Text("User Access", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))

            AccessRow(
                title = "Usage Access",
                subtitle = if (usageAccessGranted) "Granted" else "Required for deeper optimization data",
                granted = usageAccessGranted,
                buttonLabel = if (usageAccessGranted) "Manage" else "Enable",
                enabled = !isBusy,
                onClick = onOpenUsageAccessSettings
            )
            Spacer(Modifier.height(10.dp))

            AccessRow(
                title = "Battery Optimization Exemption",
                subtitle = if (batteryOptimizationIgnored) "Allowed" else "Allow app to stay active in background",
                granted = batteryOptimizationIgnored,
                buttonLabel = if (batteryOptimizationIgnored) "Manage" else "Enable",
                enabled = !isBusy,
                onClick = onOpenBatteryOptimizationSettings
            )
            Spacer(Modifier.height(10.dp))

            AccessRow(
                title = "Notification Access",
                subtitle = if (notificationPermissionGranted) "Granted" else "Needed for progress notifications",
                granted = notificationPermissionGranted,
                buttonLabel = if (notificationPermissionGranted) "Manage" else "Enable",
                enabled = !isBusy,
                onClick = onRequestNotificationPermission
            )

            Spacer(Modifier.height(32.dp))
            Text("System Optimizer v1.0.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
        }
    }
}

@Composable
private fun AccessRow(
    title: String,
    subtitle: String,
    granted: Boolean,
    buttonLabel: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Medium)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(12.dp))
            OutlinedButton(
                onClick = onClick,
                enabled = enabled
            ) {
                Text(buttonLabel)
            }
        }
    }
}
