package com.system.optimizer.core.common

import org.junit.Assert.assertEquals
import org.junit.Test

class BytesFormatterTest {

    @Test
    fun zeroBytesReturnsZeroB() {
        assertEquals("0 B", BytesFormatter.toReadable(0L))
    }

    @Test
    fun negativeBytesAreTreatedAsZero() {
        assertEquals("0 B", BytesFormatter.toReadable(-512L))
    }

    @Test
    fun smallByteCountUsesPlainBytes() {
        assertEquals("512 B", BytesFormatter.toReadable(512L))
    }

    @Test
    fun kilobytesAreFormattedWithOneDecimal() {
        assertEquals("2.0 KB", BytesFormatter.toReadable(2048L))
    }

    @Test
    fun megabytesScaleCorrectly() {
        // 5_500_000 / 1024 = 5371.09 KB -> 5.2 MB
        assertEquals("5.2 MB", BytesFormatter.toReadable(5_500_000L))
    }

    @Test
    fun gigabyteScaleSnapsToOneDecimal() {
        val oneAndAHalfGib = 1_610_612_736L // 1.5 GiB exactly
        assertEquals("1.5 GB", BytesFormatter.toReadable(oneAndAHalfGib))
    }
}
