package com.example.planer.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.planer.Nav
import com.example.planer.data.AppState
import com.example.planer.data.Event
import com.example.planer.data.EventType
import com.example.planer.data.Fmt
import com.example.planer.ui.components.BackHeader
import com.example.planer.ui.components.CircleButton
import com.example.planer.ui.components.ErrorNote
import com.example.planer.ui.components.Field
import com.example.planer.ui.components.Page
import com.example.planer.ui.components.Pill
import com.example.planer.ui.components.PrimaryButton
import com.example.planer.ui.components.SettingRow
import com.example.planer.ui.components.T
import com.example.planer.ui.components.Toggle
import com.example.planer.ui.components.card
import com.example.planer.ui.theme.LocalP
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

@Composable
fun EventEditScreen(app: AppState, nav: Nav, id: Long?, date: LocalDate) {
    val c = LocalP.current
    val ctx = LocalContext.current
    val existing = remember(id) { app.data.events.firstOrNull { it.id == id } }

    var title by remember { mutableStateOf(existing?.title ?: "") }
    var type by remember { mutableStateOf(existing?.type ?: EventType.BIRTHDAY) }
    var day by remember { mutableStateOf(existing?.date ?: date) }
    var time by remember { mutableStateOf(existing?.time) }
    var remindAt by remember { mutableStateOf(existing?.remind ?: "day") }
    var yearly by remember { mutableStateOf(existing?.yearly ?: true) }
    var note by remember { mutableStateOf(existing?.note ?: "") }
    var confirmDelete by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val bg = if (c.dark) Brush.verticalGradient(listOf(Color(0xFF261D36), Color(0xFF221A2A), c.bg))
    else Brush.verticalGradient(listOf(Color(0xFFE4D8FA), Color(0xFFF7DDEE), c.bg))
    val cardBg = if (c.dark) c.card else Color.White.copy(alpha = 0.9f)

    Box(Modifier.fillMaxSize()) {
        Page(bg, bottom = 24.dp, gap = 16.dp, bottomBar = if (error != null) 140.dp else 88.dp) {
            BackHeader(if (existing == null) "Новое событие" else "Событие", { nav.pop() }) {
                if (existing != null) {
                    CircleButton("trash", if (confirmDelete) "Подтвердить удаление" else "Удалить событие", {
                        if (confirmDelete) {
                            app.deleteEvent(existing.id)
                            nav.pop()
                        } else confirmDelete = true
                    }, bg = if (confirmDelete) c.danger else c.card, tint = if (confirmDelete) c.card else c.danger)
                }
            }
            if (confirmDelete) T("Нажми на корзину ещё раз, чтобы удалить", 13, FontWeight.SemiBold, c.danger, Modifier.padding(start = 4.dp))

            Column(
                Modifier.fillMaxWidth().card(cardBg, 26.dp).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                T("Название", 13, FontWeight.Bold, c.muted)
                Field(title, {
                    title = it
                    error = null
                }, "Например, день рождения Кати")
            }

            Column(
                Modifier.fillMaxWidth().card(cardBg, 26.dp).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                T("Тип", 13, FontWeight.Bold, c.muted)
                EventType.entries.forEach { t ->
                    val on = t == type
                    Row(
                        Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(18.dp))
                            .background(if (on) c.chip else c.card)
                            .border(2.dp, if (on) c.accent else c.line, RoundedCornerShape(18.dp))
                            .clickable(role = Role.RadioButton) {
                                type = t
                                yearly = t == EventType.BIRTHDAY
                            }
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(Modifier.width(18.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(c.typeLine(t)))
                        T(t.title, 15, FontWeight.Bold)
                    }
                }
            }

            Column(
                Modifier.fillMaxWidth().card(cardBg, 26.dp).padding(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SettingRow("Дата") {
                    Box(
                        Modifier.height(40.dp).clip(RoundedCornerShape(14.dp)).background(c.chip)
                            .clickable(role = Role.Button) {
                                DatePickerDialog(ctx, { _, y, m, dd -> day = LocalDate.of(y, m + 1, dd) }, day.year, day.monthValue - 1, day.dayOfMonth).show()
                            }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) { T(Fmt.dayShort(day) + " " + day.year, 15, FontWeight.Bold) }
                }
                SettingRow("Весь день") {
                    Toggle(time == null, "Весь день") { allDay -> time = if (allDay) null else "19:00" }
                }
                if (time != null) {
                    SettingRow("Время") {
                        Box(
                            Modifier.height(40.dp).clip(RoundedCornerShape(14.dp)).background(c.chip)
                                .clickable(role = Role.Button) {
                                    val t = time?.let { runCatching { LocalTime.parse(it) }.getOrNull() } ?: LocalTime.of(19, 0)
                                    TimePickerDialog(ctx, { _, h, m -> time = "%02d:%02d".format(h, m) }, t.hour, t.minute, true).show()
                                }
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) { T(time ?: "", 15, FontWeight.Bold) }
                    }
                }
                SettingRow("Повторять каждый год", divider = false) {
                    Toggle(yearly, "Повторять каждый год") { yearly = it }
                }
            }

            Column(
                Modifier.fillMaxWidth().card(cardBg, 26.dp).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                T("Напомнить", 13, FontWeight.Bold, c.muted)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("day" to "За день", "hour" to "За час", "none" to "Не нужно").forEach { (k, l) ->
                        Pill(l, remindAt == k, { remindAt = k }, height = 40.dp, bg = c.chip, selectedBg = c.accent)
                    }
                }
            }

            Column(
                Modifier.fillMaxWidth().card(cardBg, 26.dp).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                T("Заметка", 13, FontWeight.Bold, c.muted)
                Field(note, { note = it }, "Идеи подарка, адрес…", singleLine = false, size = 15)
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
                if (existing == null) "Добавить в календарь" else "Сохранить",
                {
                    val t = title.trim()
                    error = if (t.isEmpty()) "Напиши название события" else null
                    if (error == null) {
                        val base = existing ?: Event(0L, t, type, day)
                        val keepLook = existing != null && existing.type == type
                        val saved = base.copy(
                            title = t,
                            type = type,
                            date = day,
                            time = time,
                            note = note.trim(),
                            icon = if (keepLook) base.icon else type.icon,
                            hue = if (keepLook) base.hue else type.hue,
                            remind = remindAt,
                            yearly = yearly
                        )
                        app.saveEvent(saved)
                        // Открываем в календаре месяц, где событие видно.
                        nav.calMonth = YearMonth.from(saved.nextFrom(LocalDate.now()))
                        nav.pop()
                    }
                }
            )
        }
    }
}
