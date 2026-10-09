package com.example.planer.data

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.planer.MainActivity
import com.example.planer.R
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Напоминания. Будильник всегда стоит на ближайшее напоминание;
 * когда он срабатывает, показываем уведомления и ставим следующий.
 */
object Reminders {
    const val CHANNEL = "planer_reminders"
    private const val EXTRA_AT = "at"

    data class Due(val at: LocalDateTime, val title: String, val text: String)

    fun createChannel(ctx: Context) {
        val nm = ctx.getSystemService(NotificationManager::class.java) ?: return
        if (nm.getNotificationChannel(CHANNEL) == null) {
            val ch = NotificationChannel(CHANNEL, "Напоминания", NotificationManager.IMPORTANCE_DEFAULT)
            ch.description = "Задачи, вода, события и цикл"
            nm.createNotificationChannel(ch)
        }
    }

    /** Все напоминания строго после [from] на ближайшие три дня. */
    fun upcoming(d: AppData, from: LocalDateTime): List<Due> {
        if (!d.onboarded) return emptyList()
        val out = mutableListOf<Due>()
        val start = from.toLocalDate()
        for (k in 0L..3L) {
            val day = start.plusDays(k)
            val due = d.dueOn(day)
            if (d.remindMorning) {
                val hello = if (d.name.isBlank()) "Доброе утро!" else "Доброе утро, ${d.name}!"
                val text = if (due.isEmpty()) "Сегодня свободный день — отдыхай." else "На сегодня дел: ${due.size}. Облачко в тебя верит."
                out += Due(day.atTime(8, 0), hello, text)
            }
            if (d.remindWater) {
                for (h in 10..20 step 2) out += Due(day.atTime(h, 0), "Время выпить воды", "Стакан воды — и дальше по плану.")
            }
            if (d.remindEvening) {
                out += Due(day.atTime(21, 0), "Вечерний итог", "Отметь, что получилось сегодня.")
            }
            due.filter { it.remind && !it.doneOn(day) }.forEach { t ->
                val exact = parseTime(t.remindAt)
                val begin = parseTime(t.time)
                if (exact != null) {
                    out += Due(day.atTime(exact), t.title, t.time?.let { "Начало в $it" } ?: "Напоминание о задаче")
                } else if (begin != null) {
                    val before = t.remindBefore.coerceAtLeast(0)
                    val text = if (before == 0) "Пора начинать · ${t.time}" else "Через ${Fmt.minutes(before)} · ${t.time}"
                    out += Due(day.atTime(begin).minusMinutes(before.toLong()), t.title, text)
                }
            }
            d.events.forEach { e ->
                if (e.remind == "none") return@forEach
                val on = e.nextFrom(day) == day
                if (!on) return@forEach
                val time = parseTime(e.time)
                if (e.remind == "day") {
                    out += Due(day.minusDays(1).atTime(10, 0), "Завтра: ${e.title}", e.time?.let { "В $it" } ?: e.type.title)
                } else {
                    val at = if (time != null) day.atTime(time).minusHours(1) else day.atTime(9, 0)
                    out += Due(at, e.title, if (e.time != null) "Через час · ${e.time}" else "Сегодня")
                }
            }
        }
        val cs = d.cycleStart
        if (d.remindCycle && cs != null && d.cycleLen > 0) {
            val next: LocalDate = cs.plusDays(d.cycleLen.toLong())
            out += Due(next.minusDays(2).atTime(10, 0), "Скоро новый цикл", "Примерно через 2 дня. Позаботься о себе.")
        }
        return out.filter { it.at.isAfter(from) }.sortedBy { it.at }
    }

    /** Ставит будильник на ближайшее напоминание после [from]. */
    fun scheduleNext(ctx: Context, from: LocalDateTime = LocalDateTime.now()) {
        val app = ctx.applicationContext
        val am = app.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(app, ReminderReceiver::class.java)
        val next = upcoming(AppState.get(app).data, from).firstOrNull()
        if (next == null) {
            val old = PendingIntent.getBroadcast(app, 0, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
            if (old != null) am.cancel(old)
            return
        }
        val millis = next.at.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        intent.putExtra(EXTRA_AT, millis)
        val pi = PendingIntent.getBroadcast(app, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pi)
    }

    internal fun fire(ctx: Context, atMillis: Long) {
        val at = LocalDateTime.ofInstant(Instant.ofEpochMilli(atMillis), ZoneId.systemDefault()).truncatedTo(ChronoUnit.MINUTES)
        val data = AppState.get(ctx).data
        val now = upcoming(data, at.minusMinutes(1)).filter { it.at.truncatedTo(ChronoUnit.MINUTES) == at }
        now.forEach { show(ctx, it) }
        scheduleNext(ctx, at)
    }

    private fun show(ctx: Context, due: Due) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        createChannel(ctx)
        val open = PendingIntent.getActivity(
            ctx, 1, Intent(ctx, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = NotificationCompat.Builder(ctx, CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(due.title)
            .setContentText(due.text)
            .setColor(0xFF8E74E0.toInt())
            .setAutoCancel(true)
            .setContentIntent(open)
            .build()
        try {
            NotificationManagerCompat.from(ctx).notify((due.title + due.at).hashCode(), n)
        } catch (e: SecurityException) {
            // Разрешение отозвали — просто молчим.
        }
    }

    private fun parseTime(s: String?): LocalTime? =
        if (s == null) null else try { LocalTime.parse(s) } catch (e: Exception) { null }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val at = intent.getLongExtra("at", 0L)
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || at == 0L) {
            Reminders.scheduleNext(context)
            if (intent.action == Intent.ACTION_BOOT_COMPLETED) AppState.get(context).restoreTimer()
        } else {
            Reminders.fire(context, at)
        }
    }
}
