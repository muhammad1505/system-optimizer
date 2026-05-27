package com.system.optimizer.core.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class HistoryEntry(
    val action: String,
    val result: String,
    val timestamp: String
)

@Composable
fun HistoryScreen(history: List<HistoryEntry>, modifier: Modifier = Modifier) {
    val failedCount = history.count {
        val lower = it.result.lowercase()
        "failed" in lower || "error" in lower
    }
    val successCount = history.size - failedCount

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Optimization History", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "Review recent optimization executions and outcomes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HistoryStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Total",
                    value = history.size.toString(),
                    tint = MaterialTheme.colorScheme.primary
                )
                HistoryStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Success",
                    value = successCount.toString(),
                    tint = Color(0xFF2E7D32)
                )
                HistoryStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Failed",
                    value = failedCount.toString(),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        if (history.isEmpty()) {
            item {
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "No optimization history yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(history) { entry ->
                val isFailed = entry.result.contains("failed", ignoreCase = true) ||
                    entry.result.contains("error", ignoreCase = true)

                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val icon = if (isFailed) Icons.Default.ErrorOutline else Icons.Default.CheckCircle
                                val tint = if (isFailed) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
                                Text(
                                    entry.action,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            HistoryStatusChip(
                                status = if (isFailed) "Failed" else "Success",
                                tint = if (isFailed) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                            )
                        }

                        Text(
                            entry.result,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            entry.timestamp,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryStatCard(
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    OutlinedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = tint)
        }
    }
}

@Composable
private fun HistoryStatusChip(status: String, tint: Color) {
    Surface(
        color = tint.copy(alpha = 0.12f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = tint
        )
    }
}
