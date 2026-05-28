package com.system.optimizer.core.ui.model

import org.json.JSONArray
import org.json.JSONObject

/**
 * Pure (side-effect-free) JSON encoder/decoder for [HistoryEntry]. Lives outside the
 * ViewModel so it can be unit-tested without Android framework or Hilt.
 */
object HistorySerializer {

    fun encode(entries: List<HistoryEntry>): String {
        val array = JSONArray()
        for (entry in entries) {
            val obj = JSONObject()
                .put("action", entry.action)
                .put("result", entry.result)
                .put("timestamp", entry.timestamp)
                .put("isFailure", entry.isFailure)
            array.put(obj)
        }
        return array.toString()
    }

    fun decode(raw: String): List<HistoryEntry> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    add(
                        HistoryEntry(
                            action = obj.optString("action"),
                            result = obj.optString("result"),
                            timestamp = obj.optString("timestamp"),
                            isFailure = obj.optBoolean("isFailure", false)
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }
}
