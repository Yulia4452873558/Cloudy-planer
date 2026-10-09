package com.example.planer.data

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object Fmt {
    val RU: Locale = Locale.forLanguageTag("ru")
    private val dayFull = DateTimeFormatter.ofPattern("EEEE, d MMMM", RU)
    private val dayShort = DateTimeFormatter.ofPattern("d MMMM", RU)

    val months = listOf("Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь")
    val monthsShort = listOf("ЯНВ", "ФЕВ", "МАР", "АПР", "МАЙ", "ИЮН", "ИЮЛ", "АВГ", "СЕН", "ОКТ", "НОЯ", "ДЕК")
    val weekShort = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

    fun dayFull(d: LocalDate): String = dayFull.format(d)
    fun dayShort(d: LocalDate): String = dayShort.format(d)

    fun plural(n: Int, one: String, few: String, many: String): String {
        val m10 = n % 10
        val m100 = n % 100
        return when {
            m10 == 1 && m100 != 11 -> one
            m10 in 2..4 && (m100 < 12 || m100 > 14) -> few
            else -> many
        }
    }

    fun days(n: Int) = "$n " + plural(n, "день", "дня", "дней")

    fun minutes(totalMin: Int): String {
        if (totalMin < 60) return "$totalMin мин"
        val h = totalMin / 60
        val m = totalMin % 60
        return if (m == 0) "$h ч" else "$h ч $m мин"
    }

    fun hours(h: Double): String {
        val r = Math.round(h * 10) / 10.0
        val s = if (r == Math.floor(r)) r.toLong().toString() else r.toString()
        return s.replace('.', ',')
    }

    fun mmss(sec: Int): String {
        val m = sec / 60
        val s = sec % 60
        return "%02d:%02d".format(m, s)
    }

    fun range(time: String?, duration: Int): String {
        if (time == null) return if (duration > 0) minutes(duration) else "Без времени"
        return try {
            val start = LocalTime.parse(time)
            if (duration <= 0) {
                short(start)
            } else {
                val end = start.plusMinutes(duration.toLong())
                "${short(start)}–${short(end)} · ${minutes(duration)}"
            }
        } catch (e: Exception) {
            time
        }
    }

    private fun short(t: LocalTime): String = "${t.hour}:%02d".format(t.minute)

    fun countdown(n: Long): String = when {
        n < 0 -> "Уже прошло"
        n == 0L -> "Сегодня"
        n == 1L -> "Завтра"
        else -> "Через " + days(n.toInt())
    }

    fun greeting(hour: Int): String = when {
        hour < 5 -> "Доброй ночи"
        hour < 12 -> "Доброе утро"
        hour < 18 -> "Добрый день"
        else -> "Добрый вечер"
    }
}
