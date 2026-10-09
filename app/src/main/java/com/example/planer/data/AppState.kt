package com.example.planer.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Единое состояние приложения. Живёт столько же, сколько процесс. */
class AppState private constructor(private val ctx: Context) {

    private val prefs: SharedPreferences = ctx.getSharedPreferences("planer", Context.MODE_PRIVATE)

    var data by mutableStateOf(Codec.decode(prefs.getString(KEY, null)))
        private set

    fun update(f: (AppData) -> AppData) {
        data = f(data)
        prefs.edit().putString(KEY, Codec.encode(data)).apply()
        try {
            Reminders.scheduleNext(ctx)
        } catch (e: Exception) {
            // Напоминания не должны ломать сохранение.
        }
    }

    /** Отмечаем, что сегодня приложение открывали (для серии дней). */
    fun markVisit() {
        val today = LocalDate.now()
        if (data.onboarded && today !in data.visits) update { it.copy(visits = it.visits + today) }
    }

    // ---------- Задачи ----------

    fun toggleTask(id: Long, day: LocalDate) = update { d ->
        d.copy(tasks = d.tasks.map { t ->
            if (t.id != id) t else t.copy(done = if (day in t.done) t.done - day else t.done + day)
        })
    }

    fun saveTask(t: Task) = update { d ->
        if (t.id == 0L) {
            d.copy(tasks = d.tasks + t.copy(id = d.nextId), nextId = d.nextId + 1)
        } else {
            d.copy(tasks = d.tasks.map { if (it.id == t.id) t else it })
        }
    }

    fun deleteTask(id: Long) = update { d -> d.copy(tasks = d.tasks.filterNot { it.id == id }) }

    /** Вернуть удалённую задачу (кнопка «Вернуть»). */
    fun restoreTask(t: Task) = update { d ->
        if (d.tasks.any { it.id == t.id }) d else d.copy(tasks = d.tasks + t)
    }

    fun addCategory(title: String, hue: Hue, icon: String): String {
        val id = "c" + data.nextId
        update { d ->
            d.copy(customCats = d.customCats + Category(id, title, hue, icon), nextId = d.nextId + 1)
        }
        return id
    }

    fun deleteCategory(id: String) = update { d ->
        d.copy(
            customCats = d.customCats.filterNot { it.id == id },
            tasks = d.tasks.map { if (it.cat == id) it.copy(cat = "health") else it }
        )
    }

    // ---------- События ----------

    fun saveEvent(e: Event) = update { d ->
        if (e.id == 0L) {
            d.copy(events = d.events + e.copy(id = d.nextId), nextId = d.nextId + 1)
        } else {
            d.copy(events = d.events.map { if (it.id == e.id) e else it })
        }
    }

    fun deleteEvent(id: Long) = update { d -> d.copy(events = d.events.filterNot { it.id == id }) }

    // ---------- Занятия для фокуса ----------

    fun saveActivity(a: Activity) = update { d ->
        if (a.id.isEmpty()) {
            val na = a.copy(id = "a" + d.nextId, palette = d.acts.size % Acts.palettes.size)
            d.copy(acts = d.acts + na, nextId = d.nextId + 1)
        } else {
            d.copy(acts = d.acts.map { if (it.id == a.id) a else it })
        }
    }

    fun deleteActivity(id: String) {
        if (data.acts.size <= 1) return
        if (tAct == id) pickAct(data.acts.first { it.id != id })
        update { d -> d.copy(acts = d.acts.filterNot { it.id == id }) }
    }

    // ---------- Фокус-таймер ----------
    // Таймер хранит не «сколько осталось», а момент окончания (endAt).
    // Поэтому он не сбивается, когда приложение закрыто: при возврате время
    // просто пересчитывается. Состояние сохраняется в настройках, а уведомление
    // с обратным отсчётом рисует сама система (см. FocusTimer.kt).

    private val tPrefs: SharedPreferences = ctx.getSharedPreferences("planer_timer", Context.MODE_PRIVATE)

    var tAct by mutableStateOf(tPrefs.getString("act", null) ?: data.acts.firstOrNull()?.id ?: "work")
        private set
    var tTotal by mutableIntStateOf(tPrefs.getInt("total", (data.acts.firstOrNull()?.mins ?: 25) * 60))
        private set
    var tLeft by mutableIntStateOf(tPrefs.getInt("left", tTotal))
        private set
    var tRunning by mutableStateOf(tPrefs.getBoolean("running", false))
        private set
    /** Секунды текущей сессии, ещё не записанные в статистику. */
    var tPending by mutableIntStateOf(0)
        private set
    var tFinished by mutableStateOf(tPrefs.getBoolean("finished", false))
        private set

    /** Когда закончится сессия (мс), если таймер идёт. */
    var tEndAt: Long = tPrefs.getLong("endAt", 0L)
        private set
    /** До какого момента время уже записано в статистику. */
    private var lastCredit: Long = tPrefs.getLong("lastCredit", 0L)

    private fun saveTimer() {
        tPrefs.edit()
            .putString("act", tAct)
            .putInt("total", tTotal)
            .putInt("left", tLeft)
            .putBoolean("running", tRunning)
            .putBoolean("finished", tFinished)
            .putLong("endAt", tEndAt)
            .putLong("lastCredit", lastCredit)
            .apply()
    }

    /** Записывает в статистику время, прошедшее с прошлой записи. */
    private fun credit(now: Long, finished: Boolean) {
        val until = minOf(now, tEndAt)
        val sec = ((until - lastCredit) / 1000L).toInt()
        if (sec > 0) lastCredit += sec * 1000L
        tPending = 0
        if (sec <= 0 && !finished) return
        val act = tAct
        val day = LocalDate.now()
        update { d ->
            val today = d.focus[day].orEmpty()
            val add = sec.coerceAtLeast(0)
            val nd = today + (act to ((today[act] ?: 0) + add))
            d.copy(focus = d.focus + (day to nd), sessions = d.sessions + if (finished) 1 else 0)
        }
    }

    fun startPause() {
        if (tRunning) pause() else start()
    }

    fun start() {
        if (tRunning) return
        if (tLeft <= 0) tLeft = tTotal
        val now = System.currentTimeMillis()
        tEndAt = now + tLeft * 1000L
        lastCredit = now
        tFinished = false
        tRunning = true
        saveTimer()
        FocusTimer.onStarted(ctx, this)
    }

    fun pause() {
        if (!tRunning) return
        val now = System.currentTimeMillis()
        if (now >= tEndAt) {
            finish()
            return
        }
        credit(now, false)
        tLeft = ((tEndAt - now + 999L) / 1000L).toInt()
        tRunning = false
        saveTimer()
        FocusTimer.onPaused(ctx, this)
    }

    /** Остановить и сбросить таймер (кнопка «Отмена» и «Сбросить»). */
    fun cancel() {
        if (tRunning) credit(System.currentTimeMillis(), false)
        tRunning = false
        tFinished = false
        tLeft = tTotal
        tPending = 0
        saveTimer()
        FocusTimer.onCancelled(ctx)
    }

    /** Сессия дошла до конца. */
    fun finish() {
        if (!tRunning) return
        credit(tEndAt, true)
        tRunning = false
        tFinished = true
        tLeft = 0
        saveTimer()
        FocusTimer.onFinished(ctx, this)
    }

    /** Вызывается каждые 250 мс, пока открыт экран: пересчитывает оставшееся время. */
    fun tick() {
        if (!tRunning) return
        val now = System.currentTimeMillis()
        if (now >= tEndAt) {
            finish()
            return
        }
        tLeft = ((tEndAt - now + 999L) / 1000L).toInt()
        tPending = ((now - lastCredit) / 1000L).toInt().coerceAtLeast(0)
        if (tPending >= 30) {
            credit(now, false)
            saveTimer()
        }
    }

    /** Записать накопленное время (при сворачивании приложения). */
    fun flush(finished: Boolean) {
        if (tRunning) {
            credit(System.currentTimeMillis(), finished)
            saveTimer()
        }
    }

    fun setDuration(mins: Int) {
        if (tRunning) credit(System.currentTimeMillis(), false)
        val wasRunning = tRunning
        tRunning = false
        tFinished = false
        tTotal = mins * 60
        tLeft = tTotal
        saveTimer()
        if (wasRunning) FocusTimer.onCancelled(ctx)
    }

    fun pickAct(a: Activity) {
        setDuration(a.mins)
        tAct = a.id
        saveTimer()
    }

    fun resetTimer() = cancel()

    /** При запуске приложения: если сессия закончилась, пока приложение было закрыто. */
    fun restoreTimer() {
        if (!tRunning) return
        if (System.currentTimeMillis() >= tEndAt) {
            finish()
        } else {
            tick()
            FocusTimer.onStarted(ctx, this) // вернуть уведомление и будильник, если их смахнули
        }
    }

    fun focusSeconds(day: LocalDate, actId: String): Int {
        val saved = data.focus[day]?.get(actId) ?: 0
        return saved + if (actId == tAct && day == LocalDate.now()) tPending else 0
    }

    fun focusSecondsAll(day: LocalDate): Int =
        (data.focus[day]?.values?.sum() ?: 0) + if (day == LocalDate.now()) tPending else 0

    /** Запрос открыть вкладку «Фокус» (из уведомления). */
    var openFocusRequest by mutableIntStateOf(0)

    companion object {
        private const val KEY = "planer_data"
        @Volatile private var instance: AppState? = null

        fun get(ctx: Context): AppState = instance ?: synchronized(this) {
            instance ?: AppState(ctx.applicationContext).also { instance = it }
        }
    }
}

// ---------- Вычисления над данными ----------

fun AppData.dueOn(day: LocalDate): List<Task> =
    tasks.filter { it.dueOn(day) }.sortedWith(compareBy<Task> { it.time ?: "99:99" }.thenBy { it.id })

fun AppData.dayClosed(day: LocalDate): Boolean {
    val due = tasks.filter { it.dueOn(day) }
    return due.isNotEmpty() && due.all { it.doneOn(day) }
}

/** Сколько дней подряд (по сегодня включительно) открывали приложение. */
fun AppData.streak(today: LocalDate): Int {
    var d = today
    var n = 0
    while (d in visits) {
        n++
        d = d.minusDays(1)
    }
    return maxOf(n, if (onboarded) 1 else 0)
}

/** Самая длинная серия дней подряд. */
fun AppData.record(today: LocalDate): Int {
    var best = 0
    var cur = 0
    var prev: LocalDate? = null
    for (d in visits.sorted()) {
        cur = if (prev != null && prev.plusDays(1) == d) cur + 1 else 1
        if (cur > best) best = cur
        prev = d
    }
    return maxOf(best, streak(today))
}

fun AppData.totalDone(): Int = tasks.sumOf { it.done.size }

fun AppData.waterGoalDays(): Int = water.count { it.value * 250 >= waterGoal }

/** Очки: за выполненные задачи, фокус-сессии, норму воды и дни с приложением. */
fun AppData.points(): Int = totalDone() * 5 + sessions * 20 + waterGoalDays() * 10 + maxOf(visits.size - 1, 0) * 5

fun AppData.level(): Int = points() / 200 + 1

/** День цикла (1 — первый день) или null, если начало не отмечено. */
fun AppData.cycleDay(today: LocalDate): Int? {
    val start = cycleStart ?: return null
    val diff = ChronoUnit.DAYS.between(start, today).toInt()
    return if (diff < 0) null else diff + 1
}
