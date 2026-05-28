package com.system.optimizer.core.data.source.local

import android.content.Context
import android.content.SharedPreferences
import com.system.optimizer.core.common.Constants.PREF_AUTO_OPTIMIZE
import com.system.optimizer.core.common.Constants.PREF_DARK_MODE
import com.system.optimizer.core.common.Constants.PREF_HISTORY_JSON
import com.system.optimizer.core.common.Constants.PREF_LAST_OPTIMIZE
import com.system.optimizer.core.common.Constants.PREF_NAME
import com.system.optimizer.core.common.Constants.PREF_TOTAL_OPTIMIZED
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lightweight wrapper around [SharedPreferences] for app-level preferences and a small
 * serialized history payload. Persistence layer is intentionally synchronous so it can be
 * mocked easily in unit tests; the volume is tiny (kilobytes max).
 */
@Singleton
class LocalDataSource @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    var lastOptimize: Long
        get() = prefs.getLong(PREF_LAST_OPTIMIZE, 0L)
        set(value) {
            prefs.edit().putLong(PREF_LAST_OPTIMIZE, value).apply()
        }

    var totalOptimized: Int
        get() = prefs.getInt(PREF_TOTAL_OPTIMIZED, 0)
        set(value) {
            prefs.edit().putInt(PREF_TOTAL_OPTIMIZED, value).apply()
        }

    var isDarkMode: Boolean
        get() = prefs.getBoolean(PREF_DARK_MODE, false)
        set(value) {
            prefs.edit().putBoolean(PREF_DARK_MODE, value).apply()
        }

    var isAutoOptimize: Boolean
        get() = prefs.getBoolean(PREF_AUTO_OPTIMIZE, false)
        set(value) {
            prefs.edit().putBoolean(PREF_AUTO_OPTIMIZE, value).apply()
        }

    /**
     * Raw JSON for the history list. The viewmodel is responsible for (de)serializing it
     * because we want to keep [LocalDataSource] free of Compose / domain types.
     */
    var historyJson: String
        get() = prefs.getString(PREF_HISTORY_JSON, "") ?: ""
        set(value) {
            prefs.edit().putString(PREF_HISTORY_JSON, value).apply()
        }

    fun clearHistory() {
        prefs.edit().remove(PREF_HISTORY_JSON).apply()
    }
}
