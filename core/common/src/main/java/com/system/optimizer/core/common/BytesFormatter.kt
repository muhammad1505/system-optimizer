package com.system.optimizer.core.common

import java.util.Locale

/**
 * Utility functions for formatting raw byte counts into human-friendly strings.
 *
 * Pure / side-effect free so it can be unit-tested without Android dependencies.
 */
object BytesFormatter {

    private val UNITS = listOf("B", "KB", "MB", "GB", "TB")

    /**
     * Format [bytes] using IEC-style 1024 progression. Negative or zero input returns "0 B".
     *
     * Examples:
     *  - 0 -> "0 B"
     *  - 512 -> "512 B"
     *  - 2048 -> "2.0 KB"
     *  - 5_500_000 -> "5.2 MB"
     */
    fun toReadable(bytes: Long): String {
        if (bytes <= 0L) return "0 B"
        var value = bytes.toDouble()
        var idx = 0
        while (value >= 1024 && idx < UNITS.lastIndex) {
            value /= 1024
            idx += 1
        }
        val shown = if (idx == 0) {
            value.toInt().toString()
        } else {
            String.format(Locale.US, "%.1f", value)
        }
        return "$shown ${UNITS[idx]}"
    }
}
