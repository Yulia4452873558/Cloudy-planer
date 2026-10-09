package com.example.planer.data

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/** Сохранение данных приложения в JSON (SharedPreferences). */
object Codec {

    fun encode(d: AppData): String {
        val o = JSONObject()
        o.put("v", 2)
        o.put("onboarded", d.onboarded)
        o.put("name", d.name)
        o.put("tasks", JSONArray().apply { d.tasks.forEach { put(taskOut(it)) } })
        o.put("events", JSONArray().apply { d.events.forEach { put(eventOut(it)) } })
        o.put("customCats", JSONArray().apply {
            d.customCats.forEach { c ->
                put(JSONObject().apply {
                    put("id", c.id); put("title", c.title); put("hue", c.hue.name); put("icon", c.icon)
                })
            }
        })
        o.put("acts", JSONArray().apply {
            d.acts.forEach { a ->
                put(JSONObject().apply {
                    put("id", a.id); put("title", a.title); put("emoji", a.emoji)
                    put("mins", a.mins); put("goal", a.goalHours); put("palette", a.palette)
                })
            }
        })
        o.put("visits", JSONArray().apply { d.visits.forEach { put(it.toString()) } })
        o.put("water", JSONObject().apply { d.water.forEach { (k, v) -> put(k.toString(), v) } })
        o.put("mood", JSONObject().apply { d.mood.forEach { (k, v) -> put(k.toString(), v) } })
        o.put("sleep", JSONObject().apply { d.sleep.forEach { (k, v) -> put(k.toString(), v) } })
        o.put("cycleStart", d.cycleStart?.toString() ?: "")
        o.put("cycleLen", d.cycleLen)
        o.put("focus", JSONObject().apply {
            d.focus.forEach { (day, map) ->
                put(day.toString(), JSONObject().apply { map.forEach { (a, s) -> put(a, s) } })
            }
        })
        o.put("sessions", d.sessions)
        o.put("waterGoal", d.waterGoal)
        o.put("theme", d.theme.name)
        o.put("remindMorning", d.remindMorning)
        o.put("remindWater", d.remindWater)
        o.put("remindEvening", d.remindEvening)
        o.put("remindCycle", d.remindCycle)
        o.put("nextId", d.nextId)
        return o.toString()
    }

    fun decode(s: String?): AppData {
        if (s.isNullOrBlank()) return AppData()
        return try {
            val o = JSONObject(s)
            if (o.optInt("v", 1) < 2) {
                // Первая версия заполняла приложение примерами — начинаем с чистого листа.
                return AppData(
                    onboarded = o.optBoolean("onboarded"),
                    name = o.optString("name"),
                    theme = enumOr<ThemeMode>(o.optString("theme")) ?: ThemeMode.LIGHT
                )
            }
            val tasks = mutableListOf<Task>()
            o.optJSONArray("tasks")?.let { a -> for (i in 0 until a.length()) taskIn(a.getJSONObject(i))?.let { tasks.add(it) } }
            val events = mutableListOf<Event>()
            o.optJSONArray("events")?.let { a -> for (i in 0 until a.length()) eventIn(a.getJSONObject(i))?.let { events.add(it) } }
            val cats = mutableListOf<Category>()
            o.optJSONArray("customCats")?.let { a ->
                for (i in 0 until a.length()) {
                    val c = a.getJSONObject(i)
                    cats.add(Category(c.optString("id"), c.optString("title"), enumOr<Hue>(c.optString("hue")) ?: Hue.LILAC, c.optString("icon", "star")))
                }
            }
            val acts = mutableListOf<Activity>()
            o.optJSONArray("acts")?.let { a ->
                for (i in 0 until a.length()) {
                    val c = a.getJSONObject(i)
                    acts.add(
                        Activity(
                            c.optString("id"), c.optString("title"), c.optString("emoji", "⭐"),
                            c.optInt("mins", 25), c.optInt("goal", 5), c.optInt("palette", 0)
                        )
                    )
                }
            }
            val visits = mutableSetOf<LocalDate>()
            o.optJSONArray("visits")?.let { a -> for (i in 0 until a.length()) parseDate(a.optString(i))?.let { visits.add(it) } }
            val focus = mutableMapOf<LocalDate, Map<String, Int>>()
            o.optJSONObject("focus")?.let { f ->
                f.keys().forEach { key ->
                    val day = parseDate(key) ?: return@forEach
                    val inner = f.getJSONObject(key)
                    val m = mutableMapOf<String, Int>()
                    inner.keys().forEach { an -> m[an.lowercase()] = inner.optInt(an) }
                    focus[day] = m
                }
            }
            AppData(
                onboarded = o.optBoolean("onboarded"),
                name = o.optString("name"),
                tasks = tasks,
                events = events,
                customCats = cats,
                acts = if (o.has("acts")) acts else Acts.defaults,
                visits = visits,
                water = intMap(o.optJSONObject("water")),
                mood = strMap(o.optJSONObject("mood")),
                sleep = intMap(o.optJSONObject("sleep")),
                cycleStart = parseDate(o.optString("cycleStart")),
                cycleLen = o.optInt("cycleLen", 28),
                focus = focus,
                sessions = o.optInt("sessions"),
                waterGoal = o.optInt("waterGoal", 2000),
                theme = enumOr<ThemeMode>(o.optString("theme")) ?: ThemeMode.LIGHT,
                remindMorning = o.optBoolean("remindMorning", true),
                remindWater = o.optBoolean("remindWater", true),
                remindEvening = o.optBoolean("remindEvening", false),
                remindCycle = o.optBoolean("remindCycle", true),
                nextId = o.optLong("nextId", 1L)
            )
        } catch (e: Exception) {
            AppData()
        }
    }

    private fun taskOut(t: Task) = JSONObject().apply {
        put("id", t.id)
        put("title", t.title)
        put("cat", t.cat)
        put("habit", t.habit)
        put("icon", t.icon)
        put("hue", t.hue.name)
        put("date", t.date?.toString() ?: "")
        put("days", JSONArray().apply { t.days.forEach { put(it) } })
        put("time", t.time ?: "")
        put("duration", t.duration)
        put("remind", t.remind)
        put("remindBefore", t.remindBefore)
        put("remindAt", t.remindAt ?: "")
        put("note", t.note)
        put("done", JSONArray().apply { t.done.forEach { put(it.toString()) } })
    }

    private fun taskIn(o: JSONObject): Task? {
        val cat = o.optString("cat").lowercase().ifBlank { return null }
        val days = mutableSetOf<Int>()
        o.optJSONArray("days")?.let { a -> for (i in 0 until a.length()) days.add(a.optInt(i)) }
        val done = mutableSetOf<LocalDate>()
        o.optJSONArray("done")?.let { a -> for (i in 0 until a.length()) parseDate(a.optString(i))?.let { done.add(it) } }
        return Task(
            id = o.optLong("id"),
            title = o.optString("title"),
            cat = cat,
            habit = o.optBoolean("habit"),
            icon = o.optString("icon", "star"),
            hue = enumOr<Hue>(o.optString("hue")) ?: Hue.LILAC,
            date = parseDate(o.optString("date")),
            days = days,
            time = o.optString("time").ifBlank { null },
            duration = o.optInt("duration"),
            remind = o.optBoolean("remind"),
            remindBefore = o.optInt("remindBefore", 10),
            remindAt = o.optString("remindAt").ifBlank { null },
            note = o.optString("note"),
            done = done
        )
    }

    private fun eventOut(e: Event) = JSONObject().apply {
        put("id", e.id)
        put("title", e.title)
        put("type", e.type.name)
        put("date", e.date.toString())
        put("time", e.time ?: "")
        put("note", e.note)
        put("icon", e.icon)
        put("hue", e.hue.name)
        put("remind", e.remind)
        put("yearly", e.yearly)
    }

    private fun eventIn(o: JSONObject): Event? {
        val type = enumOr<EventType>(o.optString("type")) ?: return null
        val date = parseDate(o.optString("date")) ?: return null
        return Event(
            id = o.optLong("id"),
            title = o.optString("title"),
            type = type,
            date = date,
            time = o.optString("time").ifBlank { null },
            note = o.optString("note"),
            icon = o.optString("icon", type.icon),
            hue = enumOr<Hue>(o.optString("hue")) ?: type.hue,
            remind = o.optString("remind", "day"),
            yearly = o.optBoolean("yearly")
        )
    }

    private fun intMap(o: JSONObject?): Map<LocalDate, Int> {
        if (o == null) return emptyMap()
        val m = mutableMapOf<LocalDate, Int>()
        o.keys().forEach { k -> parseDate(k)?.let { m[it] = o.optInt(k) } }
        return m
    }

    private fun strMap(o: JSONObject?): Map<LocalDate, String> {
        if (o == null) return emptyMap()
        val m = mutableMapOf<LocalDate, String>()
        o.keys().forEach { k -> parseDate(k)?.let { m[it] = o.optString(k) } }
        return m
    }

    private fun parseDate(s: String?): LocalDate? =
        if (s.isNullOrBlank()) null else try { LocalDate.parse(s) } catch (e: Exception) { null }

    private inline fun <reified T : Enum<T>> enumOr(name: String?): T? =
        enumValues<T>().firstOrNull { it.name == name }
}
