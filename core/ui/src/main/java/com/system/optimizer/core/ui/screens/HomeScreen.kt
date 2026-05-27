package com.system.optimizer.core.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.system.optimizer.core.ui.theme.*

enum class OptimizationStepStatus {
    PENDING, RUNNING, COMPLETED
}

data class OptimizationStepUi(
    val key: String,
    val title: String,
    val description: String,
    val status: OptimizationStepStatus = OptimizationStepStatus.PENDING,
    val result: String = ""
)

@Composable
fun HomeScreen(
    isOptimizing: Boolean,
    progressMessage: String,
    progressPercent: Float,
    progressSteps: List<OptimizationStepUi>,
    onOptimizeRamClick: () -> Unit,
    onClearCacheClick: () -> Unit,
    onOptimizeBatteryClick: () -> Unit,
    onKillProcessesClick: () -> Unit,
    onOptimizeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            val completedCount = progressSteps.count { it.status == OptimizationStepStatus.COMPLETED }
            val totalCount = progressSteps.size

            Text("System Optimizer", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                text = if (isOptimizing) "Optimizing in progress" else "Ready to optimize your device",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(16.dp))

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Optimization Progress",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                progressMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "${(progressPercent * 100).toInt()}%",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = progressPercent.coerceIn(0f, 1f),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatBadge("Completed", "$completedCount/$totalCount", Modifier.weight(1f))
                        StatBadge("Mode", if (isOptimizing) "Running" else "Standby", Modifier.weight(1f))
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Detailed Steps", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(10.dp))
                    progressSteps.forEachIndexed { index, step ->
                        ProgressStepRow(step = step)
                        if (index < progressSteps.lastIndex) {
                            Spacer(Modifier.height(8.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            OptimizeCard(
                title = "RAM Optimizer",
                desc = "Free up memory",
                icon = Icons.Default.Memory,
                color = BlueInfo,
                status = progressSteps.firstOrNull { it.key == "ram" }?.status ?: OptimizationStepStatus.PENDING,
                enabled = !isOptimizing,
                onClick = onOptimizeRamClick
            )
            Spacer(Modifier.height(12.dp))
            OptimizeCard(
                title = "Cache Cleaner",
                desc = "Clear junk files",
                icon = Icons.Default.DeleteSweep,
                color = OrangeWarning,
                status = progressSteps.firstOrNull { it.key == "cache" }?.status ?: OptimizationStepStatus.PENDING,
                enabled = !isOptimizing,
                onClick = onClearCacheClick
            )
            Spacer(Modifier.height(12.dp))
            OptimizeCard(
                title = "Battery Saver",
                desc = "Optimize battery",
                icon = Icons.Default.BatteryChargingFull,
                color = GreenOptimize,
                status = progressSteps.firstOrNull { it.key == "battery" }?.status ?: OptimizationStepStatus.PENDING,
                enabled = !isOptimizing,
                onClick = onOptimizeBatteryClick
            )
            Spacer(Modifier.height(12.dp))
            OptimizeCard(
                title = "Process Manager",
                desc = "Kill background apps",
                icon = Icons.Default.Close,
                color = RedAlert,
                status = progressSteps.firstOrNull { it.key == "process" }?.status ?: OptimizationStepStatus.PENDING,
                enabled = !isOptimizing,
                onClick = onKillProcessesClick
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onOptimizeAllClick,
                enabled = !isOptimizing,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenOptimize)
            ) {
                if (isOptimizing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("OPTIMIZING...", fontWeight = FontWeight.Bold)
                } else {
                    Text("OPTIMIZE NOW", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StatBadge(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        shape = MaterialTheme.shapes.small
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ProgressStepRow(step: OptimizationStepUi) {
    val (icon, tint, statusLabel) = when (step.status) {
        OptimizationStepStatus.PENDING -> Triple(Icons.Default.RadioButtonUnchecked, MaterialTheme.colorScheme.onSurfaceVariant, "Pending")
        OptimizationStepStatus.RUNNING -> Triple(Icons.Default.Autorenew, BlueInfo, "Running")
        OptimizationStepStatus.COMPLETED -> Triple(Icons.Default.CheckCircle, GreenOptimize, "Done")
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = statusLabel,
                tint = tint,
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(step.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(step.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (step.result.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        step.result,
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                statusLabel,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun OptimizeCard(
    title: String,
    desc: String,
    icon: ImageVector,
    color: Color,
    status: OptimizationStepStatus,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val statusIcon = when (status) {
        OptimizationStepStatus.PENDING -> Icons.Default.Schedule
        OptimizationStepStatus.RUNNING -> Icons.Default.Autorenew
        OptimizationStepStatus.COMPLETED -> Icons.Default.CheckCircle
    }
    val statusTint = when (status) {
        OptimizationStepStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
        OptimizationStepStatus.RUNNING -> BlueInfo
        OptimizationStepStatus.COMPLETED -> GreenOptimize
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = title, modifier = Modifier.size(36.dp), tint = color)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Bold)
                    Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.7f))
                }
            }
            Icon(statusIcon, contentDescription = status.name, tint = statusTint)
        }
    }
}
