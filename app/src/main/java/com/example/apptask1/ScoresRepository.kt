package com.example.apptask1

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
class ScoresRepository(context: Context) {

    private val prefs = context.getSharedPreferences("scores_prefs", Context.MODE_PRIVATE)

    fun loadTop10(): MutableList<ScoreRecord> {
        val json = prefs.getString("scores", "[]") ?: "[]"
        val arr = JSONArray(json)
        val list = mutableListOf<ScoreRecord>()

        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                ScoreRecord(
                    score = o.getInt("score"),
                    timeMillis = o.getLong("timeMillis"),
                    lat = o.getDouble("lat"),
                    lng = o.getDouble("lng")
                )
            )
        }
        return list
    }

    fun addRecord(newRecord: ScoreRecord) {
        val list = loadTop10()

        list.add(newRecord)

        // sort by score DESC (highest first)
        list.sortByDescending { it.score }

        // keep only top 10
        val top10 = list.take(10)

        // save back to prefs as JSON
        val arr = JSONArray()
        top10.forEach { r ->
            val o = JSONObject()
            o.put("score", r.score)
            o.put("timeMillis", r.timeMillis)
            o.put("lat", r.lat)
            o.put("lng", r.lng)
            arr.put(o)
        }

        prefs.edit().putString("scores", arr.toString()).apply()
    }
}