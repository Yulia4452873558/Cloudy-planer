package com.example.planer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.planer.Nav
import com.example.planer.Screen
import com.example.planer.data.AppState
import com.example.planer.data.Event
import com.example.planer.data.EventType
import com.example.planer.data.Fmt
import com.example.planer.ui.components.CircleButton
import com.example.planer.ui.components.Fab
import com.example.planer.ui.components.IconTile
import com.example.planer.ui.components.Mascot
import com.example.planer.ui.components.PIcon
import com.example.planer.ui.components.Page
import com.example.planer.ui.components.T
import com.example.planer.ui.components.card
import com.example.planer.ui.theme.LocalP
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.temporal.ChronoUnit

@Composable
fun CalendarScreen(app: AppState, nav: Nav) {
    val c = LocalP.current
    val today = LocalDate.now()
    val ym = nav.calMonth
    val events = app.data.events

    val bg = if (c.dark) Brush.verticalGradient(listOf(Color(0xFF261D36), Color(0xFF221A2A), c.bg))
    else Brush.verticalGradient(listOf(Color(0xFFE4D8FA), Color(0xFFF7DDEE), c.bg))

    // События этого месяца
    val monthEvents = events.mapNotNull { e -> e.inMonth(ym)?.let { it to e } }.sortedBy { it.first }
    // Позже: ближайшие после месяца
    val afterMonth = ym.plusMonths(1).atDay(1)
    val later = events.map { it.nextFrom(afterMonth) to it }.filter { !it.first.isBefore(afterMonth) }
        .sortedBy { it.first }.take(3)

    Box(Modifier.fillMaxSize()) {
        Page(bg, bottom = 180.dp) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    T(Fmt.greeting(LocalTime.now().hour) + ",", 30, FontWeight.ExtraBold, letterSpacing = -0.5f)
                    T("Вот что впереди.", 15, FontWeight.SemiBold, c.muted)
                }
                Mascot()
            }

            // Карточка месяца
            Column(
                Modifier.fillMaxWidth().card(c.card.copy(alpha = if (c.dark) 1f else 0.9f))
                    .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircleButton("back", "Предыдущий месяц", { nav.calMonth = ym.minusMonths(1) }, bg = Color.Transparent, tint = c.muted)
                    T(Fmt.months[ym.monthValue - 1] + " " + ym.year, 17, FontWeight.ExtraBold, modifier = Modifier.weight(1f), align = androidx.compose.ui.text.style.TextAlign.Center)
                    CircleButton("chevron", "Следующий месяц", { nav.calMonth = ym.plusMonths(1) }, bg = Color.Transparent, tint = c.muted)
                }
                Row(Modifier.fillMaxWidth()) {
                    Fmt.weekShort.forEach {
                        T(it, 12, FontWeight.Bold, c.muted, Modifier.weight(1f), align = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
                val first = ym.atDay(1)
                val lead = first.dayOfWeek.value - 1
                val cells = mutableListOf<LocalDate>()
                for (i in lead downTo 1) cells.add(first.minusDays(i.toLong()))
                for (dd in 1..ym.lengthOfMonth()) cells.add(ym.atDay(dd))
                while (cells.size % 7 != 0) cells.add(cells.last().plusDays(1))
                cells.chunked(7).forEach { week ->
                    Row(Modifier.fillMaxWidth()) {
                        week.forEach { day ->
                            val inMonth = YearMonth.from(day) == ym
                            val isToday = day == today
                            val ev = if (inMonth) events.firstOrNull { it.inMonth(ym) == day } else null
                            Column(
                                Modifier.weight(1f).height(46.dp)
                                    .clickable(enabled = inMonth, role = Role.Button) { nav.push(Screen.EventEdit(ev?.id, day)) },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    Modifier.size(34.dp).clip(CircleShape)
                                        .border(2.dp, if (isToday) c.accent else Color.Transparent, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    T(
                                        day.dayOfMonth.toString(), 16,
                                        if (isToday) FontWeight.ExtraBold else if (inMonth) FontWeight.Bold else FontWeight.SemiBold,
                                        if (inMonth) c.ink else c.faint.copy(alpha = 0.6f)
                                    )
                                }
                                Box(
                                    Modifier.width(16.dp).height(3.dp).clip(RoundedCornerShape(2.dp))
                                        .background(if (ev != null) c.typeLine(ev.type) else Color.Transparent)
                                )
                            }
                        }
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally)
                ) {
                    EventType.entries.forEach { t ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(Modifier.width(14.dp).height(3.dp).clip(RoundedCornerShape(2.dp)).background(c.typeLine(t)))
                            T(t.title, 12, FontWeight.SemiBold, c.muted)
                        }
                    }
                }
            }

            // Список месяца
            Row(Modifier.padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(c.accent))
                Spacer(Modifier.width(8.dp))
                val title = if (ym == YearMonth.from(today)) "В этом месяце" else Fmt.months[ym.monthValue - 1]
                T("$title (${monthEvents.size})", 17, FontWeight.ExtraBold)
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                monthEvents.forEach { (date, e) -> EventRow(date, e, today) { nav.push(Screen.EventEdit(e.id, date)) } }
                if (monthEvents.isEmpty()) {
                    Box(Modifier.fillMaxWidth().card(c.card, 24.dp).padding(22.dp)) {
                        T("Пока пусто. Нажми «+», чтобы добавить день рождения, встречу или праздник.", 15, FontWeight.SemiBold, c.muted)
                    }
                }
            }

            if (later.isNotEmpty()) {
                T("ПОЗЖЕ", 12, FontWeight.ExtraBold, c.muted, Modifier.padding(start = 4.dp, top = 4.dp), letterSpacing = 2f)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    later.forEach { (date, e) -> EventRow(date, e, today) { nav.push(Screen.EventEdit(e.id, date)) } }
                }
            }
        }

        Fab(
            "Добавить событие",
            { nav.push(Screen.EventEdit(null, if (ym == YearMonth.from(today)) today else ym.atDay(1))) },
            Modifier.align(Alignment.BottomEnd)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(end = 20.dp, bottom = 104.dp)
        )
    }
}

@Composable
private fun EventRow(date: LocalDate, e: Event, today: LocalDate, onClick: () -> Unit) {
    val c = LocalP.current
    val tc = c.typeText(e.type)
    val cardBg = if (c.dark) c.card else Color.White.copy(alpha = 0.92f)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(
            Modifier.width(58.dp).height(76.dp).card(cardBg, 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            T(Fmt.monthsShort[date.monthValue - 1], 11, FontWeight.ExtraBold, tc, letterSpacing = 0.5f)
            T(date.dayOfMonth.toString(), 24, FontWeight.ExtraBold, tc, lineHeight = 26)
        }
        Row(
            Modifier.weight(1f).height(76.dp).card(cardBg, 24.dp)
                .clickable(onClickLabel = "Изменить", role = Role.Button, onClick = onClick)
                .padding(start = 12.dp, end = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconTile(e.icon, c.soft(e.hue))
            Column(Modifier.weight(1f)) {
                T(e.title, 16, FontWeight.ExtraBold, maxLines = 1)
                T(Fmt.countdown(ChronoUnit.DAYS.between(today, date)), 13, FontWeight.Bold, tc)
                val note = listOfNotNull(e.time, e.note.ifBlank { null }).joinToString(" · ")
                if (note.isNotEmpty()) T(note, 13, FontWeight.SemiBold, c.muted, maxLines = 1)
            }
            PIcon("chevron", c.faint, 18.dp, stroke = 2.2f)
        }
    }
}
