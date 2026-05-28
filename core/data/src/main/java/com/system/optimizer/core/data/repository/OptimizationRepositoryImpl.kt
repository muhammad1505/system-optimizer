package com.system.optimizer.core.data.repository

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.BatteryManager
import android.os.PowerManager
import com.system.optimizer.core.common.Result
import com.system.optimizer.core.data.source.local.LocalDataSource
import com.system.optimizer.core.domain.model.OptimizationResult
import com.system.optimizer.core.domain.model.OptimizationSummary
import com.system.optimizer.core.domain.repository.OptimizationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default implementation backed by Android system services. All long-running calls are kept
 * lightweight; callers are expected to dispatch to [kotlinx.coroutines.Dispatchers.IO] when
 * appropriate (the viewmodel does this).
 */
@Singleton
class OptimizationRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
    @ApplicationContext private val appContext: Context
) : OptimizationRepository {

    private val activityManager: ActivityManager by lazy {
        appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    }

    private val batteryManager: BatteryManager by lazy {
        appContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    }

    private val powerManager: PowerManager by lazy {
        appContext.getSystemService(Context.POWER_SERVICE) as PowerManager
    }

    private fun readMemoryInfo(): ActivityManager.MemoryInfo {
        val info = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(info)
        return info
    }

    private fun cacheRoots(): List<File> {
        val roots = mutableListOf<File>()
        roots += appContext.cacheDir
        roots += appContext.codeCacheDir
        appContext.externalCacheDir?.let { roots += it }
        return roots
    }

    private fun fileSize(file: File): Long {
        if (!file.exists()) return 0L
        if (file.isFile) return file.length()
        val children = file.listFiles() ?: return 0L
        var size = 0L
        for (child in children) {
            size += fileSize(child)
        }
        return size
    }

    private fun deleteChildren(root: File) {
        val children = root.listFiles() ?: return
        for (child in children) {
            child.deleteRecursively()
        }
    }

    override fun getRamInfo(): Flow<Result<List<OptimizationResult>>> = flow {
        emit(Result.Loading)
        val memory = readMemoryInfo()
        emit(
            Result.Success(
                listOf(
                    OptimizationResult(
                        type = "ram",
                        name = "System RAM",
                        description = "Available memory snapshot",
                        size = memory.availMem,
                        isOptimized = false,
                        iconRes = 0
                    )
                )
            )
        )
    }

    override fun getCacheInfo(): Flow<Result<List<OptimizationResult>>> = flow {
        emit(Result.Loading)
        val totalCache = cacheRoots().sumOf(::fileSize)
        emit(
            Result.Success(
                listOf(
                    OptimizationResult(
                        type = "cache",
                        name = "App Cache",
                        description = "Current cache footprint",
                        size = totalCache,
                        isOptimized = false,
                        iconRes = 0
                    )
                )
            )
        )
    }

    override fun getBatteryInfo(): Flow<Result<List<OptimizationResult>>> = flow {
        emit(Result.Loading)
        val level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            .coerceAtLeast(0)
        val saver = powerManager.isPowerSaveMode
        emit(
            Result.Success(
                listOf(
                    OptimizationResult(
                        type = "battery",
                        name = "Battery Saver",
                        description = if (saver) "Power saver is currently enabled"
                        else "Power saver is currently disabled",
                        size = level.toLong(),
                        isOptimized = false,
                        iconRes = 0
                    )
                )
            )
        )
    }

    override fun getRunningProcesses(): Flow<Result<List<OptimizationResult>>> = flow {
        emit(Result.Loading)
        val processCount = activityManager.runningAppProcesses?.size ?: 0
        emit(
            Result.Success(
                listOf(
                    OptimizationResult(
                        type = "process",
                        name = "Background Processes",
                        description = "Visible process snapshot",
                        size = processCount.toLong(),
                        isOptimized = false,
                        iconRes = 0
                    )
                )
            )
        )
    }

    override suspend fun optimizeRam(): Result<Long> {
        return try {
            val before = readMemoryInfo().availMem
            repeat(3) {
                Runtime.getRuntime().gc()
                delay(120)
            }
            val after = readMemoryInfo().availMem
            Result.Success((after - before).coerceAtLeast(0L))
        } catch (t: Throwable) {
            Result.Error(t)
        }
    }

    override suspend fun clearCache(): Result<Long> {
        return try {
            val roots = cacheRoots()
            val before = roots.sumOf(::fileSize)
            roots.forEach(::deleteChildren)
            val after = roots.sumOf(::fileSize)
            Result.Success((before - after).coerceAtLeast(0L))
        } catch (t: Throwable) {
            Result.Error(t)
        }
    }

    override suspend fun killBackgroundProcesses(): Result<Int> {
        return try {
            val candidates = collectRunningPackageNames()
            val killed = candidates.count { tryKillBackgroundProcess(it) }
            Result.Success(killed)
        } catch (t: Throwable) {
            Result.Error(t)
        }
    }

    private fun collectRunningPackageNames(): Set<String> {
        val candidates = mutableSetOf<String>()
        val processes = activityManager.runningAppProcesses.orEmpty()
        for (process in processes) {
            val fromList = process.pkgList.orEmpty()
            if (fromList.isNotEmpty()) {
                candidates.addAll(fromList)
            } else {
                process.processName.substringBefore(":")
                    .takeIf { it.isNotBlank() }
                    ?.let { candidates += it }
            }
        }
        return candidates
    }

    private fun tryKillBackgroundProcess(pkg: String): Boolean {
        if (pkg == appContext.packageName) return false
        return try {
            val info = appContext.packageManager.getApplicationInfo(pkg, 0)
            if ((info.flags and ApplicationInfo.FLAG_SYSTEM) != 0) return false
            activityManager.killBackgroundProcesses(pkg)
            true
        } catch (_: Throwable) {
            // Ignore packages not accessible on current Android restrictions.
            false
        }
    }

    override suspend fun optimizeBattery(): Result<Int> {
        return try {
            val beforeMem = readMemoryInfo().availMem
            repeat(2) {
                Runtime.getRuntime().gc()
                delay(120)
            }
            val afterMem = readMemoryInfo().availMem
            val memFreed = (afterMem - beforeMem).coerceAtLeast(0L)

            val batteryPercent = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                .coerceAtLeast(0)
            val bonus = if (powerManager.isPowerSaveMode) 4 else 0
            val estimatedSaving = (memFreed / (64L * 1024L * 1024L)).toInt() +
                bonus +
                if (batteryPercent < 20) 2 else 0

            Result.Success(estimatedSaving.coerceAtMost(15))
        } catch (t: Throwable) {
            Result.Error(t)
        }
    }

    override fun getOptimizationHistory(): Flow<List<OptimizationSummary>> = flow {
        emit(emptyList())
    }

    override suspend fun saveOptimizationSummary(summary: OptimizationSummary) {
        localDataSource.lastOptimize = System.currentTimeMillis()
        localDataSource.totalOptimized += 1
    }
}
