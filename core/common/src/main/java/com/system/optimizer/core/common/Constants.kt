package com.system.optimizer.core.common

object Constants {
    const val APP_NAME = "System Optimizer"
    const val VERSION_CODE = 1
    const val VERSION_NAME = "1.0.0"
    
    // Optimization types
    const val OPTIMIZE_TYPE_RAM = "ram"
    const val OPTIMIZE_TYPE_CACHE = "cache"
    const val OPTIMIZE_TYPE_BATTERY = "battery"
    const val OPTIMIZE_TYPE_JUNK = "junk"
    
    // Preference keys
    const val PREF_NAME = "system_optimizer_prefs"
    const val PREF_LAST_OPTIMIZE = "last_optimize"
    const val PREF_TOTAL_OPTIMIZED = "total_optimized"
    const val PREF_DARK_MODE = "dark_mode"
    
    // Time constants
    const val ONE_MINUTE_MS = 60_000L
    const val ONE_HOUR_MS = 3_600_000L
    const val ONE_DAY_MS = 86_400_000L
}
