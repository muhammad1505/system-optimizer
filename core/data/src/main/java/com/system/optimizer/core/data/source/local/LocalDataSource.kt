package com.system.optimizer.core.data.source.local

import android.content.Context
import android.content.SharedPreferences
import com.system.optimizer.core.common.Constants.PREF_DARK_MODE
import com.system.optimizer.core.common.Constants.PREF_LAST_OPTIMIZE
import com.system.optimizer.core.common.Constants.PREF_NAME
import com.system.optimizer.core.common.Constants.PREF_TOTAL_OPTIMIZED
import javax.inject.Inject

class LocalDataSource @Inject constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    var lastOptimize: Long
        get() = prefs.getLong(PREF_LAST_OPTIMIZE, 0L)
        set(value) = prefs.edit().putLong(PREF_LAST_OPTIMIZE, value).apply()
    
    var totalOptimized: Int
        get() = prefs.getInt(PREF_TOTAL_OPTIMIZED, 0)
        set(value) = prefs.edit().putInt(PREF_TOTAL_OPTIMIZED, value).apply()
    
    var isDarkMode: Boolean
        get() = prefs.getBoolean(PREF_DARK_MODE, false)
        set(value) = prefs.edit().putBoolean(PREF_DARK_MODE, value).apply()
}
