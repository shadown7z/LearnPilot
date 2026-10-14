package com.atlaes.learnpilot.data

import android.content.Context
import com.atlaes.learnpilot.core.Goal
import com.atlaes.learnpilot.core.Review
import com.atlaes.learnpilot.core.StudyTask
import com.atlaes.learnpilot.core.Subject
import org.json.JSONArray
import org.json.JSONObject

data class Snapshot(val goals: List<Goal> = emptyList(), val activeId: String = "", val tasks: List<StudyTask> = emptyList(), val reviews: List<Review> = emptyList(), val dark: Boolean = false)

class LocalStore(context: Context) {
    private val prefs = context.getSharedPreferences("learnpilot_v1", Context.MODE_PRIVATE)
    fun load(): Snapshot = try {
        val raw = prefs.getString("data", null) ?: return Snapshot()
        val root = JSONObject(raw)
        val goals = root.optJSONArray("goals")?.objects()?.map { g ->
            Goal(g.getString("id"), g.getString("name"), g.getString("deadline"), g.getInt("minutes"), g.getJSONArray("subjects").objects().map { s -> Subject(s.getString("id"), s.getString("name"), s.optInt("level", 2), s.optInt("importance", 3), s.optString("weakness")) }, g.optInt("workDays", 6), g.optString("kind", "自定义"))
        } ?: emptyList()
        val tasks = root.optJSONArray("tasks")?.objects()?.map { t -> StudyTask(t.getString("id"), t.getString("subjectId"), t.getString("title"), t.getString("steps"), t.getString("method"), t.getInt("minutes"), t.getString("due"), t.optBoolean("done"), t.optInt("actualMinutes"), t.optInt("mastery")) } ?: emptyList()
        val reviews = root.optJSONArray("reviews")?.objects()?.map { r -> Review(r.getString("id"), r.getString("taskId"), r.getString("subjectId"), r.getString("title"), r.getString("nextDate"), r.optInt("repetitions"), r.optInt("lastResult")) } ?: emptyList()
        Snapshot(goals, root.optString("activeId"), tasks, reviews, root.optBoolean("dark"))
    } catch (_: Exception) { Snapshot() }
    fun save(data: Snapshot) {
        val root = JSONObject().put("activeId", data.activeId).put("dark", data.dark)
        root.put("goals", JSONArray().apply { data.goals.forEach { g -> put(JSONObject().put("id", g.id).put("name", g.name).put("deadline", g.deadline).put("minutes", g.minutesPerDay).put("workDays", g.workDays).put("kind", g.kind).put("subjects", JSONArray().apply { g.subjects.forEach { s -> put(JSONObject().put("id", s.id).put("name", s.name).put("level", s.level).put("importance", s.importance).put("weakness", s.weakness)) } })) } })
        root.put("tasks", JSONArray().apply { data.tasks.forEach { t -> put(JSONObject().put("id", t.id).put("subjectId", t.subjectId).put("title", t.title).put("steps", t.steps).put("method", t.method).put("minutes", t.minutes).put("due", t.due).put("done", t.done).put("actualMinutes", t.actualMinutes).put("mastery", t.mastery)) } })
        root.put("reviews", JSONArray().apply { data.reviews.forEach { r -> put(JSONObject().put("id", r.id).put("taskId", r.taskId).put("subjectId", r.subjectId).put("title", r.title).put("nextDate", r.nextDate).put("repetitions", r.repetitions).put("lastResult", r.lastResult)) } })
        prefs.edit().putString("data", root.toString()).apply()
    }
    fun clear() = prefs.edit().clear().apply()
    private fun JSONArray.objects() = (0 until length()).map { getJSONObject(it) }
}
