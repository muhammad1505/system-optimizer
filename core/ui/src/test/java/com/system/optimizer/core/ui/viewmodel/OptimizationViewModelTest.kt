package com.system.optimizer.core.ui.viewmodel

import app.cash.turbine.test
import com.system.optimizer.core.common.Result
import com.system.optimizer.core.data.source.local.LocalDataSource
import com.system.optimizer.core.domain.usecase.ClearCacheUseCase
import com.system.optimizer.core.domain.usecase.KillProcessesUseCase
import com.system.optimizer.core.domain.usecase.OptimizeBatteryUseCase
import com.system.optimizer.core.domain.usecase.OptimizeRamUseCase
import com.system.optimizer.core.ui.model.OptimizationStepStatus
import com.system.optimizer.core.ui.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OptimizationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val localDataSource: LocalDataSource = mockk(relaxed = true)
    private val optimizeRamUseCase: OptimizeRamUseCase = mockk()
    private val clearCacheUseCase: ClearCacheUseCase = mockk()
    private val optimizeBatteryUseCase: OptimizeBatteryUseCase = mockk()
    private val killProcessesUseCase: KillProcessesUseCase = mockk()

    private lateinit var viewModel: OptimizationViewModel

    @Before
    fun setup() {
        every { localDataSource.isDarkMode } returns false
        every { localDataSource.isAutoOptimize } returns false
        every { localDataSource.historyJson } returns ""
        every { localDataSource.totalOptimized } returns 0
        every { localDataSource.lastOptimize } returns 0L

        viewModel = OptimizationViewModel(
            localDataSource = localDataSource,
            optimizeRamUseCase = optimizeRamUseCase,
            clearCacheUseCase = clearCacheUseCase,
            optimizeBatteryUseCase = optimizeBatteryUseCase,
            killProcessesUseCase = killProcessesUseCase,
            ioDispatcher = mainDispatcherRule.testDispatcher
        )
    }

    @Test
    fun `initial state hydrates from preferences`() = runTest {
        viewModel.uiState.test {
            val initial = awaitItem()
            assertFalse(initial.isOptimizing)
            assertFalse(initial.isDarkMode)
            assertFalse(initial.isAutoOptimize)
            assertEquals(4, initial.totalSteps)
            assertEquals(0, initial.completedSteps)
            assertEquals(0, initial.runningSteps)
            assertEquals(0, initial.failedSteps)
            assertTrue(initial.history.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setDarkMode persists value and updates state`() = runTest {
        viewModel.setDarkMode(true)

        verify { localDataSource.isDarkMode = true }
        assertTrue(viewModel.uiState.value.isDarkMode)
    }

    @Test
    fun `runSingleAction RAM success records history and increments total`() = runTest {
        coEvery { optimizeRamUseCase() } returns Result.Success(2_048L)

        viewModel.uiState.test {
            // initial state
            awaitItem()

            viewModel.runSingleAction("ram")

            // skip intermediate emissions; advance until isOptimizing flips to false
            var terminal = awaitItem()
            while (terminal.isOptimizing) {
                terminal = awaitItem()
            }

            val ramStep = terminal.progressSteps.first { it.key == "ram" }
            assertEquals(OptimizationStepStatus.COMPLETED, ramStep.status)
            assertTrue(ramStep.result.contains("Freed"))
            assertEquals(1, terminal.history.size)
            assertEquals("RAM Optimizer", terminal.history.first().action)
            assertFalse(terminal.history.first().isFailure)
            cancelAndIgnoreRemainingEvents()
        }

        verify { localDataSource.totalOptimized = 1 }
    }

    @Test
    fun `runSingleAction failure marks step failed and persists history`() = runTest {
        coEvery { clearCacheUseCase() } returns Result.Error(IllegalStateException("denied"))

        viewModel.uiState.test {
            awaitItem()
            viewModel.runSingleAction("cache")

            var terminal = awaitItem()
            while (terminal.isOptimizing) {
                terminal = awaitItem()
            }

            val cacheStep = terminal.progressSteps.first { it.key == "cache" }
            assertEquals(OptimizationStepStatus.FAILED, cacheStep.status)
            assertTrue(cacheStep.result.contains("denied"))
            assertEquals(1, terminal.failedSteps)
            assertEquals(1, terminal.history.size)
            assertTrue(terminal.history.first().isFailure)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `runOptimizeAll executes all four modules and emits final message`() = runTest {
        coEvery { optimizeRamUseCase() } returns Result.Success(1024L)
        coEvery { clearCacheUseCase() } returns Result.Success(0L)
        coEvery { optimizeBatteryUseCase() } returns Result.Success(3)
        coEvery { killProcessesUseCase() } returns Result.Success(2)

        viewModel.uiState.test {
            awaitItem()
            viewModel.runOptimizeAll()

            var terminal = awaitItem()
            while (terminal.isOptimizing) {
                terminal = awaitItem()
            }

            assertEquals(4, terminal.completedSteps)
            assertEquals(0, terminal.failedSteps)
            assertEquals(4, terminal.history.size)
            assertEquals(
                "Full optimization completed successfully",
                terminal.progressMessage
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `runSingleAction is ignored while another optimization is running`() = runTest {
        coEvery { optimizeRamUseCase() } returns Result.Success(0L)
        coEvery { clearCacheUseCase() } returns Result.Success(0L)

        viewModel.uiState.test {
            awaitItem()
            viewModel.runSingleAction("ram")
            // While the RAM job is in flight, attempt a second action — VM should ignore.
            viewModel.runSingleAction("cache")

            var terminal = awaitItem()
            while (terminal.isOptimizing) {
                terminal = awaitItem()
            }

            // Only RAM should have ran; cache stays PENDING because the second call was
            // rejected.
            val ramStep = terminal.progressSteps.first { it.key == "ram" }
            val cacheStep = terminal.progressSteps.first { it.key == "cache" }
            assertEquals(OptimizationStepStatus.COMPLETED, ramStep.status)
            assertNotEquals(OptimizationStepStatus.COMPLETED, cacheStep.status)
            assertEquals(1, terminal.history.size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
