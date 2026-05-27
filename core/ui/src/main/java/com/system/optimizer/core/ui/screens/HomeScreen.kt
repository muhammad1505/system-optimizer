package com.system.optimizer.core.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.system.optimizer.core.ui.theme.*

enum class OptimizationStepStatus {
    PENDING, RUNNING, COMPLETED, FAILED
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
    fun statusOf(key: String): OptimizationStepStatus =
        progressSteps.firstOrNull { it.key == key }?.status ?: OptimizationStepStatus.PENDING

    fun detailOf(key: String): String =
        progressSteps.firstOrNull { it.key == key }?.result.orEmpty()

    val completedCount = progressSteps.count { it.status == OptimizationStepStatus.COMPLETED }
    val failedCount = progressSteps.count { it.status == OptimizationStepStatus.FAILED }
    val runningCount = progressSteps.count { it.status == OptimizationStepStatus.RUNNING }
    val totalCount = progressSteps.size.coerceAtLeast(1)
    val healthScore = (72 + (completedCount * 7) - (failedCount * 14) - if (isOptimizing) 3 else 0).coerceIn(0, 100)
    val healthLabel = when {
        healthScore >= 90 -> "Excellent"
        healthScore >= 75 -> "Stable"
        healthScore >= 55 -> "Needs Attention"
        else -> "Critical"
    }

    val moduleItems = listOf(
        ModuleCardUi(
            key = "ram",
            title = "RAM Optimizer",
            desc = "Free inactive memory and reduce pressure",
            icon = Icons.Default.Memory,
            tint = BlueInfo,
            status = statusOf("ram"),
            statusDetail = detailOf("ram"),
            onRun = onOptimizeRamClick
        ),
        ModuleCardUi(
            key = "cache",
            title = "Cache Cleaner",
            desc = "Remove temporary and residual files",
            icon = Icons.Default.DeleteSweep,
            tint = OrangeWarning,
            status = statusOf("cache"),
            statusDetail = detailOf("cache"),
            onRun = onClearCacheClick
        ),
        ModuleCardUi(
            key = "battery",
            title = "Battery Saver",
            desc = "Tune background battery consumption",
            icon = Icons.Default.BatteryChargingFull,
            tint = GreenOptimize,
            status = statusOf("battery"),
            statusDetail = detailOf("battery"),
            onRun = onOptimizeBatteryClick
        ),
        ModuleCardUi(
            key = "process",
            title = "Process Manager",
            desc = "Stop unused background processes",
            icon = Icons.Default.Close,
            tint = RedAlert,
            status = statusOf("process"),
            statusDetail = detailOf("process"),
            onRun = onKillProcessesClick
        )
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("System Optimizer", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                text = if (isOptimizing) "Engine is running. Live metrics are updating." else "Control center ready. Run targeted optimization modules.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            DeviceHealthCard(
                score = healthScore,
                healthLabel = healthLabel,
                progressMessage = progressMessage,
                progressPercent = progressPercent,
                completedCount = completedCount,
                failedCount = failedCount,
                runningCount = runningCount,
                totalCount = totalCount
            )
        }

        item {
            SectionHeader(
                title = "Quick Actions",
                subtitle = "Run one module instantly from here"
            )
        }

        item {
            QuickActionGrid(
                enabled = !isOptimizing,
                onOptimizeRamClick = onOptimizeRamClick,
                onClearCacheClick = onClearCacheClick,
                onOptimizeBatteryClick = onOptimizeBatteryClick,
                onKillProcessesClick = onKillProcessesClick
            )
        }

        item {
            SectionHeader(
                title = "Optimization Center",
                subtitle = "Each module shows live state and latest result"
            )
        }

        items(moduleItems, key = { "module-${it.key}" }) { module ->
            OptimizeCard(
                title = module.title,
                desc = module.desc,
                icon = module.icon,
                color = module.tint,
                status = module.status,
                statusDetail = module.statusDetail,
                enabled = !isOptimizing,
                onClick = module.onRun
            )
        }

        item {
            SectionHeader(
                title = "Execution Timeline",
                subtitle = "Track what was processed in each optimization stage"
            )
        }

        items(progressSteps, key = { "timeline-${it.key}" }) { step ->
            TimelineCard(step = step)
        }

        item {
            Button(
                onClick = onOptimizeAllClick,
                enabled = !isOptimizing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                if (isOptimizing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Running Full Optimization")
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Start Full Optimization", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun DeviceHealthCard(
    score: Int,
    healthLabel: String,
    progressMessage: String,
    progressPercent: Float,
    completedCount: Int,
    failedCount: Int,
    runningCount: Int,
    totalCount: Int
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Device Health", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
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
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("$score", fontWeight = FontWeight.Bold)
                        Text(healthLabel, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            LinearProgressIndicator(
                progress = progressPercent.coerceIn(0f, 1f),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatBadge(title = "Completed", value = "$completedCount/$totalCount", modifier = Modifier.weight(1f))
                StatBadge(title = "Running", value = runningCount.toString(), modifier = Modifier.weight(1f))
                StatBadge(title = "Failed", value = failedCount.toString(), modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun QuickActionGrid(
    enabled: Boolean,
    onOptimizeRamClick: () -> Unit,
    onClearCacheClick: () -> Unit,
    onOptimizeBatteryClick: () -> Unit,
    onKillProcessesClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            QuickActionButton(
                label = "RAM",
                icon = Icons.Default.Memory,
                modifier = Modifier.weight(1f),
                enabled = enabled,
                onClick = onOptimizeRamClick
            )
            QuickActionButton(
                label = "Cache",
                icon = Icons.Default.DeleteSweep,
                modifier = Modifier.weight(1f),
                enabled = enabled,
                onClick = onClearCacheClick
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            QuickActionButton(
                label = "Battery",
                icon = Icons.Default.BatteryChargingFull,
                modifier = Modifier.weight(1f),
                enabled = enabled,
                onClick = onOptimizeBatteryClick
            )
            QuickActionButton(
                label = "Process",
                icon = Icons.Default.Close,
                modifier = Modifier.weight(1f),
                enabled = enabled,
                onClick = onKillProcessesClick
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        contentPadding = PaddingValues(horizontal = 10.dp)
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun StatBadge(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        shape = MaterialTheme.shapes.small
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
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
    statusDetail: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, onClick = onClick)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, contentDescription = title, modifier = Modifier.size(28.dp), tint = color)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                StatusBadge(status = status)
            }

            Text(
                text = if (statusDetail.isBlank()) "No execution result yet for this module." else statusDetail,
                style = MaterialTheme.typography.bodySmall,
                color = if (statusDetail.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
            )

            FilledTonalButton(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Run Module")
            }
        }
    }
}

@Composable
private fun TimelineCard(step: OptimizationStepUi) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            StatusDot(step.status)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(step.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Text(step.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val detail = if (step.result.isBlank()) stepStatusLabel(step.status) else step.result
                Text(detail, style = MaterialTheme.typography.labelMedium, color = statusColor(step.status))
            }
        }
    }
}

@Composable
private fun StatusBadge(status: OptimizationStepStatus) {
    val icon = when (status) {
        OptimizationStepStatus.PENDING -> Icons.Default.Schedule
        OptimizationStepStatus.RUNNING -> Icons.Default.Autorenew
        OptimizationStepStatus.COMPLETED -> Icons.Default.CheckCircle
        OptimizationStepStatus.FAILED -> Icons.Default.ErrorOutline
    }
    Surface(
        color = statusColor(status).copy(alpha = 0.14f),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = statusColor(status))
            Text(stepStatusLabel(status), style = MaterialTheme.typography.labelSmall, color = statusColor(status))
        }
    }
}

@Composable
private fun StatusDot(status: OptimizationStepStatus) {
    Surface(
        modifier = Modifier.size(12.dp),
        shape = MaterialTheme.shapes.small,
        color = statusColor(status)
    ) {}
}

private fun stepStatusLabel(status: OptimizationStepStatus): String = when (status) {
    OptimizationStepStatus.PENDING -> "Pending"
    OptimizationStepStatus.RUNNING -> "Running"
    OptimizationStepStatus.COMPLETED -> "Completed"
    OptimizationStepStatus.FAILED -> "Failed"
}

@Composable
private fun statusColor(status: OptimizationStepStatus): Color = when (status) {
    OptimizationStepStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
    OptimizationStepStatus.RUNNING -> BlueInfo
    OptimizationStepStatus.COMPLETED -> GreenOptimize
    OptimizationStepStatus.FAILED -> MaterialTheme.colorScheme.error
}

private data class ModuleCardUi(
    val key: String,
    val title: String,
    val desc: String,
    val icon: ImageVector,
    val tint: Color,
    val status: OptimizationStepStatus,
    val statusDetail: String,
    val onRun: () -> Unit
)
