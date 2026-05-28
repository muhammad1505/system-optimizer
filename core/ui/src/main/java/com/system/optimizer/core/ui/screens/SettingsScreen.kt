package com.system.optimizer.core.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.system.optimizer.core.common.Constants
import com.system.optimizer.core.ui.theme.AppTheme

@Composable
fun SettingsScreen(
    darkModeEnabled: Boolean,
    autoOptimizeEnabled: Boolean,
    usageAccessGranted: Boolean,
    batteryOptimizationIgnored: Boolean,
    notificationPermissionGranted: Boolean,
    isBusy: Boolean,
    totalOptimized: Int,
    onDarkModeChange: (Boolean) -> Unit,
    onAutoOptimizeChange: (Boolean) -> Unit,
    onOpenUsageAccessSettings: () -> Unit,
    onOpenBatteryOptimizationSettings: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = AppTheme.spacing
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.sm + spacing.xs)
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Personalize your optimizer and manage system permissions.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Statistics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Total successful optimization runs: $totalOptimized",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            ToggleRow(
                title = "Dark Mode",
                subtitle = "Force dark theme regardless of system setting",
                checked = darkModeEnabled,
                enabled = !isBusy,
                onCheckedChange = onDarkModeChange
            )
        }

        item {
            ToggleRow(
                title = "Auto Optimize",
                subtitle = "Automatically run a sweep when battery is low",
                checked = autoOptimizeEnabled,
                enabled = !isBusy,
                onCheckedChange = onAutoOptimizeChange
            )
        }

        item {
            Text(
                text = "User Access",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            AccessRow(
                title = "Usage Access",
                subtitle = if (usageAccessGranted) "Granted"
                else "Required for deeper optimization data",
                granted = usageAccessGranted,
                buttonLabel = if (usageAccessGranted) "Manage" else "Enable",
                enabled = !isBusy,
                onClick = onOpenUsageAccessSettings
            )
        }

        item {
            AccessRow(
                title = "Battery Optimization Exemption",
                subtitle = if (batteryOptimizationIgnored) "Allowed"
                else "Allow app to stay active in background",
                granted = batteryOptimizationIgnored,
                buttonLabel = if (batteryOptimizationIgnored) "Manage" else "Enable",
                enabled = !isBusy,
                onClick = onOpenBatteryOptimizationSettings
            )
        }

        item {
            AccessRow(
                title = "Notification Access",
                subtitle = if (notificationPermissionGranted) "Granted"
                else "Needed for progress notifications",
                granted = notificationPermissionGranted,
                buttonLabel = if (notificationPermissionGranted) "Manage" else "Enable",
                enabled = !isBusy,
                onClick = onRequestNotificationPermission
            )
        }

        item {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${Constants.APP_NAME} v${Constants.VERSION_NAME}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Medium)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(12.dp))
            Switch(
                checked = checked,
                enabled = enabled,
                onCheckedChange = onCheckedChange
            )
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Medium)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (granted) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
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
