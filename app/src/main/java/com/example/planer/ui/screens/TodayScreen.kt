package com.example.planer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.planer.Nav
import com.example.planer.Screen
import com.example.planer.data.AppState
import com.example.planer.data.Fmt
import com.example.planer.data.Task
import com.example.planer.data.dayClosed
import com.example.planer.data.dueOn
import com.example.planer.data.streak
import com.example.planer.ui.components.Bar
import com.example.planer.ui.components.CheckCircle
import com.example.planer.ui.components.Fab
import com.example.planer.ui.components.IconTile
import com.example.planer.ui.components.Mascot
import com.example.planer.ui.components.Page
import com.example.planer.ui.components.Pill
import com.example.planer.ui.components.SwipeToDelete
import com.example.planer.ui.components.NativeAdCard
import com.example.planer.ui.components.T
import com.example.planer.ui.components.card
import com.example.planer.ui.theme.LocalP
import kotlinx.coroutines.delay
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
fun TodayScreen(app: AppState, nav: Nav) {
    val c = LocalP.current
    val d = app.data
    val today = LocalDate.now()
    val sel = nav.todaySel
    var catId by remember { mutableStateOf<String?>(null) }
    var deleted by remember { mutableStateOf<Task?>(null) }

    LaunchedEffect(deleted) {
        if (deleted != null) {
            delay(4000)
            deleted = null
        }
    }

    val due = d.dueOn(sel)
    val doneCount = due.count { it.doneOn(sel) }
    val pct = if (due.isEmpty()) 0 else doneCount * 100 / due.size
    val usedCats = due.map { d.cat(it.cat) }.distinctBy { it.id }
    val activeCat = catId?.takeIf { id -> usedCats.size > 1 && usedCats.any { it.id == id } }
    val visible = due.filter { activeCat == null || d.cat(it.cat).id == activeCat }
    val streak = d.streak(today)

    val (phrase, mouth) = when {
        due.isEmpty() -> "Добавь первое дело — нажми «+» внизу" to "M15 15c2 3 8 3 10 0"
        pct == 0 -> "Облачко ждёт первое дело" to "M16 17h8"
        pct < 50 -> "Хорошее начало!" to "M16 16c1.5 1.5 6.5 1.5 8 0"
        pct < 100 -> "Больше половины, так держать!" to "M15 15c2 3 8 3 10 0"
        else -> "Все дела закрыты. Ты умница!" to "M14 14c2 5.5 10 5.5 12 0"
    }

    Box(Modifier.fillMaxSize()) {
        Page(SolidColor(c.bg), bottom = 180.dp) {
            // Заголовок
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) {
                    T(Fmt.dayFull(sel), 14, FontWeight.SemiBold, c.muted)
                    val title = when (sel) {
                        today -> "Сегодня"
                        today.plusDays(1) -> "Завтра"
                        today.minusDays(1) -> "Вчера"
                        else -> Fmt.dayShort(sel).replaceFirstChar { it.uppercase() }
                    }
                    T(title, 34, FontWeight.ExtraBold, letterSpacing = -0.5f)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        Modifier.height(44.dp).clip(RoundedCornerShape(22.dp)).background(c.card)
                            .clickable(onClickLabel = "Награды", role = Role.Button) { nav.push(Screen.Rewards) }
                            .padding(start = 6.dp, end = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconTile("flame", c.peach, size = 32.dp, radius = 16.dp, iconSize = 18.dp)
                        T(Fmt.days(streak), 15, FontWeight.ExtraBold)
                    }
                    Box(
                        Modifier.size(44.dp).clip(CircleShape)
                            .background(Brush.linearGradient(listOf(c.pink, c.lilac)))
                            .clickable(onClickLabel = "Профиль", role = Role.Button) { nav.push(Screen.Profile) }
                            .semantics { contentDescription = "Профиль и настройки" },
                        contentAlignment = Alignment.Center
                    ) {
                        T((d.name.firstOrNull() ?: 'Я').uppercase(), 17, FontWeight.ExtraBold)
                    }
                }
            }

            // Неделя
            val monday = sel.with(DayOfWeek.MONDAY)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (i in 0..6) {
                    val day = monday.plusDays(i.toLong())
                    val on = day == sel
                    val closed = day.isBefore(today) && d.dayClosed(day)
                    var m = Modifier.weight(1f).clip(RoundedCornerShape(20.dp))
                    m = if (on) m.background(c.accent) else m.background(c.card)
                    Column(
                        m.clickable(role = Role.Button) { nav.todaySel = day }.padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        T(Fmt.weekShort[i], 12, FontWeight.SemiBold, if (on) c.onAccent else c.muted)
                        Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                            if (closed && !on) {
                                IconTile("star", c.lemon, size = 28.dp, radius = 14.dp, iconSize = 16.dp)
                            } else {
                                T(day.dayOfMonth.toString(), 17, if (on) FontWeight.ExtraBold else FontWeight.Bold, if (on) c.onAccent else c.ink)
                            }
                        }
                    }
                }
            }

            // Облачко и прогресс
            Row(
                Modifier.fillMaxWidth().card(c.card).padding(start = 14.dp, end = 18.dp, top = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Mascot(mouth)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    T(phrase, 15, FontWeight.ExtraBold)
                    if (due.isNotEmpty()) {
                        Row {
                            T("Выполнено $doneCount из ${due.size}", 13, FontWeight.SemiBold, c.muted, Modifier.weight(1f))
                            T("$pct%", 13, FontWeight.ExtraBold, c.accentDeep)
                        }
                        Bar(pct / 100f, 10.dp, c.track, SolidColor(c.accent))
                    } else {
                        T("Задачи и привычки на этот день появятся здесь", 13, FontWeight.SemiBold, c.muted)
                    }
                }
            }

            // Категории — только те, что есть на этот день
            if (usedCats.size > 1) {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Pill("Все", activeCat == null, { catId = null })
                    usedCats.forEach { ct -> Pill(ct.title, activeCat == ct.id, { catId = ct.id }) }
                }
            }

            // Задачи
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                visible.forEachIndexed { i, t ->
                    key(t.id) {
                        val done = t.doneOn(sel)
                        val cat = d.cat(t.cat)
                        SwipeToDelete({
                            deleted = t
                            app.deleteTask(t.id)
                        }) {
                            Row(
                                Modifier.fillMaxWidth().card(c.soft(t.hue), 24.dp)
                                    .clickable(onClickLabel = "Изменить", role = Role.Button) { nav.push(Screen.TaskEdit(t.id, sel)) }
                                    .padding(start = 14.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconTile(t.icon, c.tile)
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    val kind = if (t.habit) "Привычка" else "Задача"
                                    val time = t.time?.let { " · $it" } ?: ""
                                    T("$kind · ${cat.title}$time", 12, FontWeight.SemiBold, c.muted, maxLines = 1)
                                    T(
                                        t.title, 17, FontWeight.Bold, if (done) c.muted else c.ink,
                                        decoration = if (done) TextDecoration.LineThrough else null, maxLines = 2
                                    )
                                }
                                CheckCircle(done, if (done) "Вернуть в список" else "Отметить выполненным") { app.toggleTask(t.id, sel) }
                            }
                        }
                    }
                    // Реклама — между второй и третьей задачей, в верхней части списка.
                    if (i == minOf(1, visible.lastIndex)) NativeAdCard("today", compact = true)
                }
                if (visible.isNotEmpty()) {
                    T("Смахни карточку влево, чтобы удалить", 12, FontWeight.SemiBold, c.faint, Modifier.fillMaxWidth(), align = TextAlign.Center)
                }
            }
        }

        Fab(
            "Добавить задачу",
            { nav.push(Screen.TaskEdit(null, sel)) },
            Modifier.align(Alignment.BottomEnd)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(end = 20.dp, bottom = 104.dp)
        )

        // «Задача удалена · Вернуть»
        val gone = deleted
        if (gone != null) {
            Row(
                Modifier.align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(start = 16.dp, end = 92.dp, bottom = 108.dp)
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(c.ink)
                    .padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                T("Задача удалена", 14, FontWeight.Bold, c.bg, Modifier.weight(1f), maxLines = 1)
                Box(
                    Modifier.height(40.dp).clip(RoundedCornerShape(14.dp))
                        .clickable(role = Role.Button) {
                            app.restoreTask(gone)
                            deleted = null
                        }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) { T("Вернуть", 14, FontWeight.ExtraBold, c.accent) }
            }
        }
    }
}
