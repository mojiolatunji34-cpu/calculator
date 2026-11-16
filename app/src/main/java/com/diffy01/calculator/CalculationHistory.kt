package com.diffy01.calculator

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class HistoryItem(val expression: String, val result: String, val timestamp: Long = System.currentTimeMillis())

class CalculationHistory(context: Context) {
    private val prefs = context.getSharedPreferences("calculator_history", Context.MODE_PRIVATE)
    fun load(): List<HistoryItem> {
        val array = runCatching { JSONArray(prefs.getString("items", "[]")) }.getOrElse { JSONArray() }
        return buildList {
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                add(HistoryItem(item.optString("expression"), item.optString("result"), item.optLong("timestamp")))
            }
        }
    }
    fun save(items: List<HistoryItem>) {
        val array = JSONArray()
        items.take(100).forEach {
            array.put(JSONObject().put("expression", it.expression).put("result", it.result).put("timestamp", it.timestamp))
        }
        prefs.edit().putString("items", array.toString()).apply()
    }
    fun clear() { prefs.edit().remove("items").apply() }
}
