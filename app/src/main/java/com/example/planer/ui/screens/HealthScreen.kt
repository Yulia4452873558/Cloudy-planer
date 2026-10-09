package com.example.planer.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.planer.Nav
import com.example.planer.Tab
import com.example.planer.data.AppState
import com.example.planer.data.Fmt
import com.example.planer.data.cycleDay
import com.example.planer.ui.components.NativeAdCard
import com.example.planer.ui.components.Bar
import com.example.planer.ui.components.CircleButton
import com.example.planer.ui.components.PIcon
import com.example.planer.ui.components.Page
import com.example.planer.ui.components.T
import com.example.planer.ui.components.card
import com.example.planer.ui.theme.LocalP
import java.time.LocalDate

private data class Mood(val id: String, val name: String, val mouth: String, val light: List<Color>)

private val moods = listOf(
    Mood("great", "Отлично", "M8 14c1 2.5 7 2.5 8 0", listOf(Color(0xFFFFF6D6), Color(0xFFFCEBC2))),
    Mood("good", "Хорошо", "M8.5 14.5c1 1.2 6 1.2 7 0", listOf(Color(0xFFEAF7DF), Color(0xFFDCF0D2))),
    Mood("ok", "Ровно", "M9 15h6", listOf(Color(0xFFE6F0FD), Color(0xFFDCE4FA))),
    Mood("tired", "Устала", "M9 15.5c1-.8 5-.8 6 0", listOf(Color(0xFFEFE8FD), Color(0xFFE3D9F9))),
    Mood("sad", "Грустно", "M8 16c1-2.5 7-2.5 8 0", listOf(Color(0xFFFDE7F3), Color(0xFFF6D8EC)))
)

@Composable
fun HealthScreen(app: AppState, nav: Nav) {
    val c = LocalP.current
    val d = app.data
    val today = LocalDate.now()
    val dark = c.dark
    val ctx = LocalContext.current
    fun g(light: List<Color>, darkC: List<Color>) = Brush.linearGradient(if (dark) darkC else light)

    Page(c.pageBrush, gap = 14.dp) {
        Column(Modifier.padding(horizontal = 4.dp)) {
            T(Fmt.dayFull(today), 14, FontWeight.SemiBold, c.muted)
            T("Здоровье", 34, FontWeight.ExtraBold, letterSpacing = -0.5f)
        }

        // Цикл
        val day = d.cycleDay(today)
        val len = d.cycleLen
        val late = if (day != null && day > len) day - len else 0
        val phase = when {
            day == null -> "Ещё не отмечен"
            late > 0 -> "Задержка " + Fmt.days(late)
            day <= 5 -> "Менструация"
            day <= 13 -> "Фолликулярная фаза"
            day <= 16 -> "Овуляция"
            else -> "Лютеиновая фаза"
        }
        val hint = when {
            day == null -> "Отметь первый день последних месячных — посчитаю прогноз"
            late > 0 -> "Если цикл начался — отметь сегодняшний день"
            day == len -> "Следующий ожидается завтра"
            else -> "Следующий через " + Fmt.days(len - day + 1)
        }
        fun pickStart() {
            val init = d.cycleStart ?: today
            val dlg = DatePickerDialog(ctx, { _, y, m, dd ->
                val picked = LocalDate.of(y, m + 1, dd)
                app.update { it.copy(cycleStart = if (picked.isAfter(today)) today else picked) }
            }, init.year, init.monthValue - 1, init.dayOfMonth)
            dlg.datePicker.maxDate = System.currentTimeMillis()
            dlg.show()
        }
        Row(
            Modifier.fillMaxWidth()
                .card(g(listOf(Color(0xFFFDE7F3), Color(0xFFF8D6EC), Color(0xFFECD8F7)), listOf(Color(0xFF3A2A3E), Color(0xFF33283F))), 30.dp)
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                val ring = Color(0xFFE58BC2)
                val track = Color.White.copy(alpha = if (dark) 0.12f else 0.85f)
                val sweep = if (day == null) 0f else 360f * minOf(day, len) / len
                Canvas(Modifier.size(120.dp)) {
                    val sw = 13.dp.toPx()
                    val inset = sw / 2 + 4.dp.toPx()
                    val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
                    drawArc(track, 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(sw))
                    if (sweep > 0f) {
                        drawArc(ring, -90f, sweep, false, Offset(inset, inset), arcSize, style = Stroke(sw, cap = StrokeCap.Round))
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    T("день", 12, FontWeight.Bold, c.muted)
                    T(day?.toString() ?: "—", 32, FontWeight.ExtraBold, lineHeight = 34)
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                T("Цикл", 18, FontWeight.ExtraBold)
                T(phase, 14, FontWeight.Bold)
                T(hint, 13, FontWeight.SemiBold, c.muted, lineHeight = 17)
                Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        Modifier.height(40.dp).clip(RoundedCornerShape(20.dp)).background(c.card)
                            .clickable(role = Role.Button) { app.update { it.copy(cycleStart = today) } }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) { T("Сегодня", 13, FontWeight.Bold) }
                    Box(
                        Modifier.height(40.dp).clip(RoundedCornerShape(20.dp)).background(c.tile)
                            .clickable(role = Role.Button) { pickStart() }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) { T(if (day == null) "Другая дата" else "Изменить", 13, FontWeight.Bold) }
                }
            }
        }

        // Реклама — между циклом и водой
        NativeAdCard("health")

        // Вода
        val glassesGoal = (d.waterGoal / 250).coerceIn(4, 16)
        val drunk = d.water[today] ?: 0
        Column(
            Modifier.fillMaxWidth()
                .card(g(listOf(Color(0xFFE6F2FD), Color(0xFFDCE6FB), Color(0xFFE4DCF8)), listOf(Color(0xFF243246), Color(0xFF2A2A45))), 30.dp)
                .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                T("Вода", 18, FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                T("${drunk * 250} из ${d.waterGoal} мл", 14, FontWeight.Bold)
            }
            (1..glassesGoal).chunked(8).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    row.forEach { i ->
                        val full = i <= drunk
                        Box(
                            Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = if (dark) 0.08f else 0.65f))
                                .clickable(role = Role.Button) {
                                    app.update { s ->
                                        val cur = s.water[today] ?: 0
                                        s.copy(water = s.water + (today to if (cur == i) i - 1 else i))
                                    }
                                }
                                .semantics { contentDescription = "Стакан $i из $glassesGoal" },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (full) PIcon("drop", Color(0xFFA9C8F5), 22.dp, filled = true)
                                PIcon("drop", Color(0xFF5A86D0), 22.dp)
                            }
                        }
                    }
                    repeat(8 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            Bar(
                drunk.toFloat() / glassesGoal, 6.dp, Color.White.copy(alpha = if (dark) 0.1f else 0.7f),
                Brush.horizontalGradient(listOf(Color(0xFFA9D4F5), Color(0xFF9AA8F0)))
            )
        }

        // Настроение
        val curMood = d.mood[today]
        Column(
            Modifier.fillMaxWidth().card(c.card, 30.dp).padding(start = 12.dp, end = 12.dp, top = 16.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            T("Как настроение?", 18, FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                moods.forEach { m ->
                    val on = m.id == curMood
                    var mod = Modifier.weight(1f).height(76.dp).clip(RoundedCornerShape(22.dp))
                    mod = if (on) {
                        mod.background(if (dark) Brush.linearGradient(listOf(c.lilac, c.pink)) else Brush.linearGradient(m.light))
                            .border(2.dp, Color(0xFFB9A6EE), RoundedCornerShape(22.dp))
                    } else mod.background(if (dark) c.chip else Color(0xFFF7F4FC))
                    Column(
                        mod.clickable(role = Role.RadioButton) { app.update { it.copy(mood = it.mood + (today to m.id)) } },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
                    ) {
                        PIcon("M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18zM9 10v.01M15 10v.01" + m.mouth, c.ink, 28.dp)
                        T(m.name, 11, FontWeight.Bold, maxLines = 1)
                    }
                }
            }
        }

        // Сон и фокус
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val sleepLogged = d.sleep[today]
            val sleep = sleepLogged ?: 480
            Column(
                Modifier.weight(1f)
                    .card(g(listOf(Color(0xFFEFE8FD), Color(0xFFE3D9F9)), listOf(Color(0xFF2F2A45), Color(0xFF2A2540))), 30.dp)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                T("Сон", 16, FontWeight.ExtraBold)
                T(if (sleepLogged == null) "Не отмечен" else "${sleep / 60} ч ${"%02d".format(sleep % 60)} мин", 22, FontWeight.ExtraBold, maxLines = 1)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircleButton("minus", "Убавить сон на 30 минут", {
                        app.update { it.copy(sleep = it.sleep + (today to if (sleepLogged == null) sleep else (sleep - 30).coerceAtLeast(0))) }
                    }, bg = c.tile)
                    CircleButton("plus", "Прибавить сон на 30 минут", {
                        app.update { it.copy(sleep = it.sleep + (today to if (sleepLogged == null) sleep else (sleep + 30).coerceAtMost(720))) }
                    }, bg = c.tile)
                }
            }
            val focusMin = app.focusSecondsAll(today) / 60
            Column(
                Modifier.weight(1f)
                    .card(g(listOf(Color(0xFFE7F6DE), Color(0xFFDCEFEF)), listOf(Color(0xFF263A30), Color(0xFF233438))), 30.dp)
                    .clickable(onClickLabel = "Открыть таймер", role = Role.Button) { nav.tab(Tab.FOCUS) }
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                T("Фокус", 16, FontWeight.ExtraBold)
                T(Fmt.minutes(focusMin), 22, FontWeight.ExtraBold, maxLines = 1)
                Row(
                    Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(16.dp)).background(c.tile),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    T("Таймер", 14, FontWeight.Bold)
                    Spacer(Modifier.width(6.dp))
                    PIcon("chevron", c.ink, 16.dp, stroke = 2.2f)
                }
            }
        }
    }
}
