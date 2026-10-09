package com.example.planer.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.planer.Nav
import com.example.planer.data.Ads
import com.example.planer.data.AppState
import com.example.planer.data.Cats
import com.example.planer.data.Fmt
import com.example.planer.data.Hue
import com.example.planer.data.Task
import com.example.planer.ui.components.BackHeader
import com.example.planer.ui.components.CircleButton
import com.example.planer.ui.components.ErrorNote
import com.example.planer.ui.components.Field
import com.example.planer.ui.components.IconTile
import com.example.planer.ui.components.PIcon
import com.example.planer.ui.components.Page
import com.example.planer.ui.components.Pill
import com.example.planer.ui.components.PrimaryButton
import com.example.planer.ui.components.Segmented
import com.example.planer.ui.components.SettingRow
import com.example.planer.ui.components.Sheet
import com.example.planer.ui.components.T
import com.example.planer.ui.components.Toggle
import com.example.planer.ui.components.card
import com.example.planer.ui.theme.LocalP
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun TaskEditScreen(app: AppState, nav: Nav, id: Long?, date: LocalDate) {
    val c = LocalP.current
    val ctx = LocalContext.current
    val d = app.data
    val existing = remember(id) { d.tasks.firstOrNull { it.id == id } }

    var title by remember { mutableStateOf(existing?.title ?: "") }
    var habit by remember { mutableStateOf(existing?.habit ?: false) }
    var catId by remember { mutableStateOf(existing?.cat ?: "health") }
    var day by remember { mutableStateOf(existing?.date ?: date) }
    var days by remember { mutableStateOf(existing?.days ?: setOf(date.dayOfWeek.value)) }
    var time by remember { mutableStateOf(existing?.time) }
    var duration by remember { mutableStateOf(existing?.duration ?: 30) }
    var remind by remember { mutableStateOf(existing?.remind ?: false) }
    var remindBefore by remember { mutableStateOf(existing?.remindBefore ?: 10) }
    var remindAt by remember { mutableStateOf(existing?.remindAt) }
    var note by remember { mutableStateOf(existing?.note ?: "") }
    var confirmDelete by remember { mutableStateOf(false) }
    var showNewCat by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val today = LocalDate.now()
    val cat = d.cat(catId)

    fun pickTime(initial: String?, default: LocalTime, onPick: (String) -> Unit) {
        val t = initial?.let { runCatching { LocalTime.parse(it) }.getOrNull() } ?: default
        TimePickerDialog(ctx, { _, h, m -> onPick("%02d:%02d".format(h, m)) }, t.hour, t.minute, true).show()
    }

    Box(Modifier.fillMaxSize()) {
        Page(c.pageBrush, bottom = 24.dp, gap = 16.dp, bottomBar = if (error != null) 140.dp else 88.dp) {
            BackHeader(if (existing == null) "Новая задача" else "Задача", { nav.pop() }) {
                if (existing != null) {
                    CircleButton("trash", if (confirmDelete) "Подтвердить удаление" else "Удалить задачу", {
                        if (confirmDelete) {
                            app.deleteTask(existing.id)
                            nav.pop()
                        } else confirmDelete = true
                    }, bg = if (confirmDelete) c.danger else c.card, tint = if (confirmDelete) c.card else c.danger)
                }
            }
            if (confirmDelete) T("Нажми на корзину ещё раз, чтобы удалить", 13, FontWeight.SemiBold, c.danger, Modifier.padding(start = 4.dp))

            Column(
                Modifier.fillMaxWidth().card(c.soft(cat.hue), 26.dp).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                T("Название", 13, FontWeight.Bold, c.muted)
                Field(title, {
                    title = it
                    error = null
                }, "Например, полить цветы", bg = c.tile, size = 18)
            }

            // Тип и категория
            Column(
                Modifier.fillMaxWidth().card(c.card, 26.dp).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                T("Тип", 13, FontWeight.Bold, c.muted)
                Segmented(listOf("Задача", "Привычка"), if (habit) 1 else 0, {
                    habit = it == 1
                    error = null
                })
                T("Категория", 13, FontWeight.Bold, c.muted)
                d.allCats.chunked(3).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { ct ->
                            val on = ct.id == catId
                            Row(
                                Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(16.dp))
                                    .background(c.soft(ct.hue))
                                    .border(2.dp, if (on) c.accentDeep else Color.Transparent, RoundedCornerShape(16.dp))
                                    .clickable(role = Role.RadioButton) { catId = ct.id }
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                PIcon(ct.icon, c.ink, 18.dp)
                                T(ct.title, 13, FontWeight.Bold, maxLines = 1)
                            }
                        }
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Pill("+ Своя категория", false, { showNewCat = true }, height = 40.dp, bg = c.chip)
                    if (d.customCats.any { it.id == catId }) {
                        Pill("Удалить «${cat.title}»", false, {
                            app.deleteCategory(catId)
                            catId = "health"
                        }, height = 40.dp, bg = c.chip)
                    }
                }
            }

            // Когда
            Column(
                Modifier.fillMaxWidth().card(c.card, 26.dp).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                T("Когда", 13, FontWeight.Bold, c.muted)
                if (!habit) {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Pill("Сегодня", day == today, { day = today }, height = 40.dp, bg = c.chip)
                        Pill("Завтра", day == today.plusDays(1), { day = today.plusDays(1) }, height = 40.dp, bg = c.chip)
                        val other = day != today && day != today.plusDays(1)
                        Pill(if (other) Fmt.dayShort(day) else "Другая дата", other, {
                            DatePickerDialog(ctx, { _, y, m, dd -> day = LocalDate.of(y, m + 1, dd) }, day.year, day.monthValue - 1, day.dayOfMonth).show()
                        }, height = 40.dp, bg = c.chip)
                    }
                } else {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Fmt.weekShort.forEachIndexed { i, l ->
                            val n = i + 1
                            val on = n in days
                            Box(
                                Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(14.dp))
                                    .background(if (on) c.accent else c.chip)
                                    .clickable(role = Role.Checkbox) {
                                        days = if (on) days - n else days + n
                                        error = null
                                    },
                                contentAlignment = Alignment.Center
                            ) { T(l, 13, FontWeight.Bold, if (on) c.onAccent else c.ink) }
                        }
                    }
                }
                SettingRow("Время", divider = false) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (time != null) {
                            CircleButton("close", "Убрать время", { time = null }, bg = c.chip, size = 36.dp, iconSize = 14.dp)
                        }
                        Box(
                            Modifier.height(40.dp).clip(RoundedCornerShape(14.dp)).background(c.chip)
                                .clickable(role = Role.Button) { pickTime(time, LocalTime.of(9, 0)) { time = it } }
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) { T(time ?: "Без времени", 15, FontWeight.Bold) }
                    }
                }
                if (time != null) {
                    T("Длительность", 13, FontWeight.Bold, c.muted)
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(15, 30, 45, 60, 90, 120).forEach { m ->
                            Pill(Fmt.minutes(m), duration == m, { duration = m }, height = 40.dp, bg = c.chip, selectedBg = c.accent)
                        }
                    }
                }
            }

            // Напоминание
            Column(
                Modifier.fillMaxWidth().card(c.card, 26.dp).padding(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val note2 = when {
                    !remind -> "Выключено"
                    remindAt != null -> "В $remindAt"
                    time == null -> "Выбери время напоминания"
                    remindBefore == 0 -> "В момент начала"
                    else -> "За ${Fmt.minutes(remindBefore)} до начала"
                }
                SettingRow("Напомнить", note2, divider = false) {
                    Toggle(remind, "Напомнить") { on ->
                        remind = on
                        if (on && time == null && remindAt == null) pickTime(null, LocalTime.of(9, 0)) { remindAt = it }
                    }
                }
                if (remind) {
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()).padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (time != null) {
                            listOf(0, 5, 10, 15, 30, 60, 120, 1440).forEach { m ->
                                val label = when (m) {
                                    0 -> "В начале"
                                    1440 -> "За день"
                                    else -> "За " + Fmt.minutes(m)
                                }
                                Pill(label, remindAt == null && remindBefore == m, {
                                    remindAt = null
                                    remindBefore = m
                                }, height = 40.dp, bg = c.chip, selectedBg = c.accent)
                            }
                        }
                        Pill(remindAt?.let { "В $it" } ?: "Своё время", remindAt != null, {
                            pickTime(remindAt, LocalTime.of(9, 0)) { remindAt = it }
                        }, height = 40.dp, bg = c.chip, selectedBg = c.accent)
                    }
                }
            }

            Column(
                Modifier.fillMaxWidth().card(c.card, 26.dp).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                T("Заметка", 13, FontWeight.Bold, c.muted)
                Field(note, { note = it }, "Например, взять коврик", singleLine = false, size = 15)
            }
        }

        Column(
            Modifier.align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                .padding(start = 16.dp, end = 16.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ErrorNote(error)
            PrimaryButton(
                "Сохранить",
                {
                    val t = title.trim()
                    error = when {
                        t.isEmpty() -> "Напиши название задачи"
                        habit && days.isEmpty() -> "Выбери хотя бы один день недели"
                    remind && time == null && remindAt == null -> "Выбери время напоминания"
                        else -> null
                    }
                    if (error == null) {
                        val base = existing ?: Task(0L, t, cat.id, habit, cat.icon, cat.hue)
                        val keepLook = existing != null && existing.cat == cat.id
                        app.saveTask(
                            base.copy(
                                title = t,
                                cat = cat.id,
                                habit = habit,
                                icon = if (keepLook) base.icon else cat.icon,
                                hue = if (keepLook) base.hue else cat.hue,
                                date = if (habit) null else day,
                                days = if (habit) days else emptySet(),
                                time = time,
                                duration = if (time == null) 0 else duration,
                                remind = remind && (remindAt != null || time != null),
                                remindBefore = remindBefore,
                                remindAt = remindAt,
                                note = note.trim()
                            )
                        )
                        // Показываем на «Сегодня» день, где эта задача видна.
                        nav.todaySel = when {
                            !habit -> day
                            nav.todaySel.dayOfWeek.value in days -> nav.todaySel
                            else -> generateSequence(LocalDate.now()) { it.plusDays(1) }.first { it.dayOfWeek.value in days }
                        }
                        nav.pop()
                        // Межстраничная реклама: после создания каждой третьей задачи.
                        val count = app.data.tasks.size
                        if (existing == null && count > 0 && count % 3 == 0) {
                            ctx.findActivity()?.let { Ads.showInterstitial(it) }
                        }
                    }
                }
            )
        }

        if (showNewCat) {
            NewCategorySheet(
                onDismiss = { showNewCat = false },
                onCreate = { name, hue, icon ->
                    catId = app.addCategory(name, hue, icon)
                    showNewCat = false
                }
            )
        }
    }
}

@Composable
private fun NewCategorySheet(onDismiss: () -> Unit, onCreate: (String, Hue, String) -> Unit) {
    val c = LocalP.current
    var name by remember { mutableStateOf("") }
    var hue by remember { mutableStateOf(Hue.LILAC) }
    var icon by remember { mutableStateOf("star") }
    var error by remember { mutableStateOf<String?>(null) }
    Sheet("Своя категория", onDismiss, footer = {
        ErrorNote(error)
        PrimaryButton("Создать", {
            val n = name.trim()
            if (n.isEmpty()) error = "Напиши название категории" else onCreate(n, hue, icon)
        })
    }) {
        IconTile(icon, c.soft(hue), size = 56.dp, radius = 20.dp, iconSize = 28.dp)
        Field(name, {
            name = it.take(20)
            error = null
        }, "Название, например «Собака»")
        T("Цвет", 13, FontWeight.Bold, c.muted)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Hue.entries.forEach { h ->
                Box(
                    Modifier.weight(1f).aspectRatio(1f).clip(CircleShape).background(c.soft(h))
                        .border(2.dp, if (h == hue) c.accentDeep else Color.Transparent, CircleShape)
                        .clickable(role = Role.RadioButton) { hue = h }
                        .semantics { contentDescription = "Цвет ${h.ordinal + 1}" }
                )
            }
        }
        T("Значок", 13, FontWeight.Bold, c.muted)
        Cats.icons.chunked(6).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { ic ->
                    Box(
                        Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(14.dp))
                            .background(if (ic == icon) c.soft(hue) else c.chip)
                            .border(2.dp, if (ic == icon) c.accentDeep else Color.Transparent, RoundedCornerShape(14.dp))
                            .clickable(role = Role.RadioButton) { icon = ic },
                        contentAlignment = Alignment.Center
                    ) { PIcon(ic, c.ink, 20.dp) }
                }
            }
        }
    }
}

private tailrec fun android.content.Context.findActivity(): android.app.Activity? = when (this) {
    is android.app.Activity -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}
