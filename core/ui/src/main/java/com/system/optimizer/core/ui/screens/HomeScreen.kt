package com.system.optimizer.core.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.system.optimizer.core.ui.theme.*

@Composable
fun HomeScreen(onOptimizeClick: () -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text("System Optimizer", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))
            OptimizeCard("RAM Optimizer", "Free up memory", Icons.Default.Memory, BlueInfo, onOptimizeClick)
            Spacer(Modifier.height(12.dp))
            OptimizeCard("Cache Cleaner", "Clear junk files", Icons.Default.DeleteSweep, OrangeWarning, onOptimizeClick)
            Spacer(Modifier.height(12.dp))
            OptimizeCard("Battery Saver", "Optimize battery", Icons.Default.BatteryChargingFull, GreenOptimize, onOptimizeClick)
            Spacer(Modifier.height(12.dp))
            OptimizeCard("Process Manager", "Kill background apps", Icons.Default.Close, RedAlert, onOptimizeClick)
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onOptimizeClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenOptimize)
            ) {
                Text("OPTIMIZE NOW", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun OptimizeCard(title: String, desc: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = title, modifier = Modifier.size(40.dp), tint = color)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
            }
        }
    }
}
