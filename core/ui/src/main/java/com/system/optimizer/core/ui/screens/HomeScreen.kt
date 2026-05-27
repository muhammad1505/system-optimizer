package com.system.optimizer.core.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import com.system.optimizer.core.ui.theme.*

@Composable
fun HomeScreen(
    isOptimizing: Boolean,
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
            Text("System Optimizer", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))
            OptimizeCard(
                title = "RAM Optimizer",
                desc = "Free up memory",
                icon = Icons.Default.Memory,
                color = BlueInfo,
                enabled = !isOptimizing,
                onClick = onOptimizeRamClick
            )
            Spacer(Modifier.height(12.dp))
            OptimizeCard(
                title = "Cache Cleaner",
                desc = "Clear junk files",
                icon = Icons.Default.DeleteSweep,
                color = OrangeWarning,
                enabled = !isOptimizing,
                onClick = onClearCacheClick
            )
            Spacer(Modifier.height(12.dp))
            OptimizeCard(
                title = "Battery Saver",
                desc = "Optimize battery",
                icon = Icons.Default.BatteryChargingFull,
                color = GreenOptimize,
                enabled = !isOptimizing,
                onClick = onOptimizeBatteryClick
            )
            Spacer(Modifier.height(12.dp))
            OptimizeCard(
                title = "Process Manager",
                desc = "Kill background apps",
                icon = Icons.Default.Close,
                color = RedAlert,
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
private fun OptimizeCard(
    title: String,
    desc: String,
    icon: ImageVector,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = title, modifier = Modifier.size(40.dp), tint = color)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
            }
        }
    }
}
