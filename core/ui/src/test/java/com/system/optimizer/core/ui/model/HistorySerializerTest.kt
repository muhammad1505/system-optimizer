package com.system.optimizer.core.ui.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistorySerializerTest {

    @Test
    fun emptyJsonReturnsEmptyList() {
        assertTrue(HistorySerializer.decode("").isEmpty())
        assertTrue(HistorySerializer.decode("   ").isEmpty())
    }

    @Test
    fun malformedJsonReturnsEmptyList() {
        assertTrue(HistorySerializer.decode("not-json{").isEmpty())
        assertTrue(HistorySerializer.decode("{\"foo\":1}").isEmpty())
    }

    @Test
    fun encodeDecodeRoundtrip() {
        val original = listOf(
            HistoryEntry(
                action = "RAM Optimizer",
                result = "Freed 12.4 MB RAM",
                timestamp = "2026-05-28 07:00:00",
                isFailure = false
            ),
            HistoryEntry(
                action = "Cache Cleaner",
                result = "Cache cleanup failed: permission denied",
                timestamp = "2026-05-28 07:01:30",
                isFailure = true
            )
        )

        val encoded = HistorySerializer.encode(original)
        val decoded = HistorySerializer.decode(encoded)

        assertEquals(original, decoded)
    }

    @Test
    fun encodeEmptyReturnsEmptyJsonArray() {
        assertEquals("[]", HistorySerializer.encode(emptyList()))
    }
}
