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

/**
 * Уведомление фокус-таймера.
 *
 * Пока таймер идёт, в шторке висит уведомление с обратным отсчётом — его
 * обновляет сама система (chronometer), поэтому приложению не нужно работать
 * в фоне. Кнопки «Пауза / Продолжить» и «Отмена» обрабатывает [TimerReceiver].
 * Конец сессии ловит будильник AlarmManager, даже если приложение закрыто.
 *
 * WorkManager здесь не подходит: он не умеет срабатывать в точное время
 * и не обновляет уведомление каждую секунду.
 */
object FocusTimer {
    private const val CH_TIMER = "focus_timer"
    private const val CH_DONE = "focus_done"
    private const val ID_TIMER = 7001
    private const val ID_DONE = 7002

    const val ACTION_PAUSE = "ru.yulia.cloudy.timer.PAUSE"
    const val ACTION_RESUME = "ru.yulia.cloudy.timer.RESUME"
    const val ACTION_CANCEL = "ru.yulia.cloudy.timer.CANCEL"
    const val ACTION_FINISH = "ru.yulia.cloudy.timer.FINISH"
    const val EXTRA_OPEN_FOCUS = "open_focus"

    private fun channels(ctx: Context) {
        val nm = ctx.getSystemService(NotificationManager::class.java) ?: return
        if (nm.getNotificationChannel(CH_TIMER) == null) {
            nm.createNotificationChannel(
                NotificationChannel(CH_TIMER, "Фокус-таймер", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "Обратный отсчёт и кнопки управления"
                    setShowBadge(false)
                }
            )
        }
        if (nm.getNotificationChannel(CH_DONE) == null) {
            nm.createNotificationChannel(
                NotificationChannel(CH_DONE, "Окончание фокус-сессии", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Сообщает, что сессия закончилась"
                }
            )
        }
    }

    private fun canNotify(ctx: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun action(ctx: Context, action: String, code: Int): PendingIntent =
        PendingIntent.getBroadcast(
            ctx, code, Intent(ctx, TimerReceiver::class.java).setAction(action),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun openApp(ctx: Context): PendingIntent =
        PendingIntent.getActivity(
            ctx, 3,
            Intent(ctx, MainActivity::class.java)
                .putExtra(EXTRA_OPEN_FOCUS, true)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun notify(ctx: Context, id: Int, b: NotificationCompat.Builder) {
        if (!canNotify(ctx)) return
        channels(ctx)
        try {
            NotificationManagerCompat.from(ctx).notify(id, b.build())
        } catch (e: SecurityException) {
            // Разрешение на уведомления отозвано.
        }
    }

    private fun base(ctx: Context, channel: String): NotificationCompat.Builder =
        NotificationCompat.Builder(ctx, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(0xFF8E74E0.toInt())
            .setContentIntent(openApp(ctx))

    fun onStarted(ctx: Context, s: AppState) {
        val title = s.data.act(s.tAct).title
        val left = (s.tEndAt - System.currentTimeMillis()).coerceAtLeast(1000L)
        notify(
            ctx, ID_TIMER,
            base(ctx, CH_TIMER)
                .setContentTitle("Фокус · $title")
                .setContentText("До конца сессии")
                .setUsesChronometer(true)
                .setChronometerCountDown(true)
                .setWhen(s.tEndAt)
                .setShowWhen(true)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setSilent(true)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setTimeoutAfter(left)
                .addAction(0, "Пауза", action(ctx, ACTION_PAUSE, 11))
                .addAction(0, "Отмена", action(ctx, ACTION_CANCEL, 12))
        )
        NotificationManagerCompat.from(ctx).cancel(ID_DONE)
        scheduleFinish(ctx, s.tEndAt)
    }

    fun onPaused(ctx: Context, s: AppState) {
        val title = s.data.act(s.tAct).title
        notify(
            ctx, ID_TIMER,
            base(ctx, CH_TIMER)
                .setContentTitle("Пауза · $title")
                .setContentText("Осталось ${Fmt.mmss(s.tLeft)}")
                .setShowWhen(false)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setSilent(true)
                .addAction(0, "Продолжить", action(ctx, ACTION_RESUME, 13))
                .addAction(0, "Отмена", action(ctx, ACTION_CANCEL, 12))
        )
        cancelFinish(ctx)
    }

    fun onCancelled(ctx: Context) {
        NotificationManagerCompat.from(ctx).cancel(ID_TIMER)
        cancelFinish(ctx)
    }

    fun onFinished(ctx: Context, s: AppState) {
        NotificationManagerCompat.from(ctx).cancel(ID_TIMER)
        cancelFinish(ctx)
        val a = s.data.act(s.tAct)
        notify(
            ctx, ID_DONE,
            base(ctx, CH_DONE)
                .setContentTitle("Сессия завершена")
                .setContentText("${a.title}: ${Fmt.minutes(s.tTotal / 60)}. Отличная работа!")
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
        )
    }

    private fun scheduleFinish(ctx: Context, at: Long) {
        val am = ctx.getSystemService(AlarmManager::class.java) ?: return
        val pi = action(ctx, ACTION_FINISH, 14)
        try {
            if (Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
            }
        } catch (e: SecurityException) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        }
    }

    private fun cancelFinish(ctx: Context) {
        ctx.getSystemService(AlarmManager::class.java)?.cancel(action(ctx, ACTION_FINISH, 14))
    }
}

/** Кнопки уведомления и будильник окончания сессии. */
class TimerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val s = AppState.get(context)
        when (intent.action) {
            FocusTimer.ACTION_PAUSE -> s.pause()
            FocusTimer.ACTION_RESUME -> s.start()
            FocusTimer.ACTION_CANCEL -> s.cancel()
            FocusTimer.ACTION_FINISH -> s.restoreTimer()
        }
    }
}
