package com.system.optimizer.core.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.system.optimizer.core.ui.model.OptimizationStepStatus
import com.system.optimizer.core.ui.model.OptimizationStepUi
import com.system.optimizer.core.ui.theme.BlueInfo
import com.system.optimizer.core.ui.theme.GreenOptimize
import com.system.optimizer.core.ui.theme.OrangeWarning
import com.system.optimizer.core.ui.theme.RedAlert
import com.system.optimizer.core.ui.viewmodel.OptimizationUiState

/**
 * Tunable-icon descriptor for one optimization module rendered as a card.
 * Static metadata only; live state comes from the ViewModel.
 */
private data class ModuleVisuals(
    val key: String,
    val icon: ImageVector,
    val tint: Color
)

private val moduleVisualsByKey: Map<String, ModuleVisuals> = listOf(
    ModuleVisuals(key = "ram", icon = Icons.Default.Memory, tint = BlueInfo),
    ModuleVisuals(key = "cache", icon = Icons.Default.DeleteSweep, tint = OrangeWarning),
    ModuleVisuals(key = "battery", icon = Icons.Default.BatteryChargingFull, tint = GreenOptimize),
    ModuleVisuals(key = "process", icon = Icons.Default.Apps, tint = RedAlert)
).associateBy { it.key }

@Composable
fun HomeScreen(
    state: OptimizationUiState,
    onRunSingle: (String) -> Unit,
    onRunAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val healthScore = computeHealthScore(state)
    val healthLabel = when {
        healthScore >= 90 -> "Excellent"
        healthScore >= 75 -> "Stable"
        healthScore >= 55 -> "Needs Attention"
        else -> "Critical"
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Header(state = state) }

        item {
            DeviceHealthCard(
                score = healthScore,
                healthLabel = healthLabel,
                progressMessage = state.progressMessage,
                progressPercent = state.progressPercent,
                completedCount = state.completedSteps,
                failedCount = state.failedSteps,
                runningCount = state.runningSteps,
                totalCount = state.totalSteps
            )
        }

        item {
            SectionHeader(
                title = "Optimization Modules",
                subtitle = "Tap a module to run it; live status updates as it progresses."
            )
        }

        items(state.progressSteps, key = { "module-${it.key}" }) { step ->
            val visuals = moduleVisualsByKey[step.key]
            if (visuals != null) {
                ModuleCard(
                    step = step,
                    icon = visuals.icon,
                    tint = visuals.tint,
                    enabled = !state.isOptimizing,
                    onRun = { onRunSingle(step.key) }
                )
            }
        }

        item {
            SectionHeader(
                title = "Execution Timeline",
                subtitle = "Track what was processed in the most recent optimization."
            )
        }

        items(state.progressSteps, key = { "timeline-${it.key}" }) { step ->
            TimelineRow(step = step)
        }

        item {
            FullOptimizeButton(
                isOptimizing = state.isOptimizing,
                onClick = onRunAll
            )
        }
    }
}

@Composable
private fun Header(state: OptimizationUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = "System Optimizer",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (state.isOptimizing) {
                "Engine running. Live metrics are updating…"
            } else {
                "Control center ready. Run a module or full sweep."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun computeHealthScore(state: OptimizationUiState): Int {
    val base = 72
    val completedBonus = state.completedSteps * 7
    val failedPenalty = state.failedSteps * 14
    val runningPenalty = if (state.isOptimizing) 3 else 0
    return (base + completedBonus - failedPenalty - runningPenalty).coerceIn(0, 100)
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
                    Text(
                        text = "Device Health",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = progressMessage,
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
                        Text(
                            text = score.toString(),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = healthLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            LinearProgressIndicator(
                progress = { progressPercent.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatBadge(
                    title = "Completed",
                    value = "$completedCount/$totalCount",
                    modifier = Modifier.weight(1f)
                )
                StatBadge(
                    title = "Running",
                    value = runningCount.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatBadge(
                    title = "Failed",
                    value = failedCount.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
private fun ModuleCard(
    step: OptimizationStepUi,
    icon: ImageVector,
    tint: Color,
    enabled: Boolean,
    onRun: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, onClick = onRun)
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
                    Icon(
                        imageVector = icon,
                        contentDescription = step.title,
                        modifier = Modifier.size(28.dp),
                        tint = tint
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = step.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = step.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                StatusBadge(status = step.status)
            }

            Text(
                text = if (step.result.isBlank()) "No execution result yet for this module."
                else step.result,
                style = MaterialTheme.typography.bodySmall,
                color = if (step.result.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.primary
            )

            FilledTonalButton(
                onClick = onRun,
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
private fun TimelineRow(step: OptimizationStepUi) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            StatusDot(step.status)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val detail = if (step.result.isBlank()) stepStatusLabel(step.status)
                else step.result
                Text(
                    text = detail,
                    style = MaterialTheme.typography.labelMedium,
                    color = statusColor(step.status)
                )
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
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = statusColor(status)
            )
            Text(
                text = stepStatusLabel(status),
                style = MaterialTheme.typography.labelSmall,
                color = statusColor(status)
            )
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

@Composable
private fun FullOptimizeButton(
    isOptimizing: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
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
