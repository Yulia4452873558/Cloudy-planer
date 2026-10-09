package com.example.planer.data

import java.time.LocalDate
import java.time.YearMonth

enum class Hue { PINK, PEACH, MINT, BLUE, LILAC, LEMON }

/** Категория задачи: встроенная или созданная пользовательницей. */
data class Category(val id: String, val title: String, val hue: Hue, val icon: String)

object Cats {
    val builtIn = listOf(
        Category("health", "Здоровье", Hue.MINT, "pulse"),
        Category("study", "Учёба", Hue.LILAC, "book"),
        Category("work", "Работа", Hue.BLUE, "work"),
        Category("home", "Дом", Hue.LEMON, "home"),
        Category("care", "Уход", Hue.PINK, "sparkle"),
        Category("sport", "Спорт", Hue.MINT, "fit"),
        Category("food", "Питание", Hue.PEACH, "food"),
        Category("shop", "Покупки", Hue.BLUE, "cart"),
        Category("money", "Финансы", Hue.LEMON, "coin"),
        Category("hobby", "Хобби", Hue.LILAC, "palette"),
        Category("family", "Близкие", Hue.PINK, "people"),
        Category("rest", "Отдых", Hue.PEACH, "leaf")
    )

    /** Иконки, которые можно выбрать для своей категории. */
    val icons = listOf("star", "heart", "sun", "moon", "leaf", "paw", "cook", "walk", "pen", "gift", "pin", "drop")
}

data class Task(
    val id: Long,
    val title: String,
    /** id категории */
    val cat: String,
    val habit: Boolean,
    val icon: String,
    val hue: Hue,
    /** Дата разовой задачи. У привычки — null. */
    val date: LocalDate? = null,
    /** Дни недели привычки: 1 = понедельник … 7 = воскресенье. */
    val days: Set<Int> = emptySet(),
    /** Время начала в формате ЧЧ:ММ или null. */
    val time: String? = null,
    /** Длительность в минутах (0 — не указана). */
    val duration: Int = 0,
    val remind: Boolean = false,
    /** За сколько минут до начала напомнить (если задано время начала). */
    val remindBefore: Int = 10,
    /** Точное время напоминания ЧЧ:ММ. Если задано — важнее, чем remindBefore. */
    val remindAt: String? = null,
    val note: String = "",
    val done: Set<LocalDate> = emptySet()
) {
    fun dueOn(d: LocalDate): Boolean = if (habit) d.dayOfWeek.value in days else date == d
    fun doneOn(d: LocalDate): Boolean = d in done
}

enum class EventType(val title: String, val hue: Hue, val icon: String) {
    BIRTHDAY("День рождения", Hue.PINK, "gift"),
    HOLIDAY("Праздник", Hue.PEACH, "star"),
    OTHER("Другое", Hue.BLUE, "pin")
}

data class Event(
    val id: Long,
    val title: String,
    val type: EventType,
    val date: LocalDate,
    val time: String? = null,
    val note: String = "",
    val icon: String = type.icon,
    val hue: Hue = type.hue,
    /** "day", "hour" или "none". */
    val remind: String = "day",
    /** Повторять каждый год (дни рождения). */
    val yearly: Boolean = false
) {
    /** Дата события в заданном месяце или null. */
    fun inMonth(ym: YearMonth): LocalDate? {
        if (!yearly) return if (YearMonth.from(date) == ym) date else null
        if (date.monthValue != ym.monthValue || ym.year < date.year) return null
        val day = minOf(date.dayOfMonth, ym.lengthOfMonth())
        return ym.atDay(day)
    }

    /** Ближайшая дата события начиная с [from] (для ежегодных) или сама дата. */
    fun nextFrom(from: LocalDate): LocalDate {
        if (!yearly || !date.isBefore(from)) return date
        var y = from.year
        while (true) {
            val ym = YearMonth.of(y, date.monthValue)
            val d = ym.atDay(minOf(date.dayOfMonth, ym.lengthOfMonth()))
            if (!d.isBefore(from)) return d
            y++
        }
    }
}

/** Занятие для фокус-таймера. */
data class Activity(
    val id: String,
    val title: String,
    val emoji: String,
    val mins: Int,
    val goalHours: Int,
    val palette: Int
)

object Acts {
    /** Градиенты карточек целей и цвет полоски прогресса. */
    val palettes: List<Pair<List<Long>, Long>> = listOf(
        listOf(0xFFF6D3F0, 0xFFECBCEA, 0xFFE2AEE8) to 0xFFD58BE2,
        listOf(0xFFD5CCF8, 0xFFBDB2F2, 0xFFADA3EE) to 0xFF8679DC,
        listOf(0xFFCDE6FA, 0xFFB9CFF6, 0xFFB7B7F2) to 0xFF7C9BE6,
        listOf(0xFFFADCCB, 0xFFF4C3D3, 0xFFE7B6E6) to 0xFFE28BB0,
        listOf(0xFFE3D8FB, 0xFFCEBFF6, 0xFFBDACF1) to 0xFF9A84E4,
        listOf(0xFFD6EFDF, 0xFFC4E3E6, 0xFFBCCDF0) to 0xFF6FA8C8,
        listOf(0xFFFBE3C4, 0xFFF7CDBE, 0xFFEFBBD6) to 0xFFE29A7C
    )

    val defaults = listOf(
        Activity("work", "Работа", "💻", 50, 20, 0),
        Activity("study", "Учёба", "🎓", 45, 10, 4),
        Activity("read", "Чтение", "📚", 25, 4, 3),
        Activity("fit", "Фитнес", "🏋️‍♀️", 30, 5, 1),
        Activity("med", "Медитация", "🧘‍♀️", 10, 2, 2),
        Activity("dog", "Прогулка с собакой", "🐕", 30, 5, 5),
        Activity("cook", "Готовка", "🍳", 40, 4, 6)
    )

    val emojis = listOf(
        "💻", "🎓", "📚", "🏋️‍♀️", "🧘‍♀️", "🐕", "🍳", "🎨",
        "🎹", "🎸", "✍️", "🧹", "🌿", "🚶‍♀️", "🏃‍♀️", "🚴‍♀️",
        "🧶", "📷", "🎮", "🗣️", "🛁", "🧠", "💼", "🌍"
    )
}

enum class ThemeMode(val title: String) { LIGHT("Светлая"), DARK("Тёмная"), AUTO("Авто") }

data class AppData(
    val onboarded: Boolean = false,
    val name: String = "",
    val tasks: List<Task> = emptyList(),
    val events: List<Event> = emptyList(),
    val customCats: List<Category> = emptyList(),
    val acts: List<Activity> = Acts.defaults,
    /** Дни, в которые открывалось приложение. */
    val visits: Set<LocalDate> = emptySet(),
    /** Выпито стаканов (по 250 мл) по дням. */
    val water: Map<LocalDate, Int> = emptyMap(),
    val mood: Map<LocalDate, String> = emptyMap(),
    /** Сон в минутах по дням. */
    val sleep: Map<LocalDate, Int> = emptyMap(),
    /** Первый день последних месячных или null, если ещё не отмечали. */
    val cycleStart: LocalDate? = null,
    val cycleLen: Int = 28,
    /** Секунды фокуса по дням и id занятия. */
    val focus: Map<LocalDate, Map<String, Int>> = emptyMap(),
    val sessions: Int = 0,
    val waterGoal: Int = 2000,
    val theme: ThemeMode = ThemeMode.LIGHT,
    val remindMorning: Boolean = true,
    val remindWater: Boolean = true,
    val remindEvening: Boolean = false,
    val remindCycle: Boolean = true,
    val nextId: Long = 1
) {
    val allCats: List<Category> get() = Cats.builtIn + customCats
    fun cat(id: String): Category = allCats.firstOrNull { it.id == id } ?: Cats.builtIn[0]
    fun act(id: String): Activity = acts.firstOrNull { it.id == id } ?: acts.firstOrNull() ?: Acts.defaults[0]
}
