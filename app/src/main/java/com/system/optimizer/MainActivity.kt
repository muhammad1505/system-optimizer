package com.system.optimizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.system.optimizer.core.ui.screens.NavigationScreen
import com.system.optimizer.core.ui.theme.SystemOptimizerTheme
import com.system.optimizer.core.ui.viewmodel.OptimizationViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Single VM scoped to this activity; the navigation children re-use this
            // instance via hiltViewModel() because the LocalViewModelStoreOwner is shared.
            val viewModel: OptimizationViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()

            SystemOptimizerTheme(darkTheme = state.isDarkMode || systemDark) {
                NavigationScreen(viewModel = viewModel)
            }
        }
    }
}
