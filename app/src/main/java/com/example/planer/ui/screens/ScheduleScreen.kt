package com.example.planer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.planer.Nav
import com.example.planer.Screen
import com.example.planer.data.AppState
import com.example.planer.data.Fmt
import com.example.planer.data.dueOn
import com.example.planer.ui.components.CheckCircle
import com.example.planer.ui.components.IconTile
import com.example.planer.ui.components.NativeAdCard
import com.example.planer.ui.components.Page
import com.example.planer.ui.components.T
import com.example.planer.ui.components.card
import com.example.planer.ui.theme.LocalP
import java.time.LocalDate

@Composable
fun ScheduleScreen(app: AppState, nav: Nav) {
    val c = LocalP.current
    val today = LocalDate.now()
    val items = app.data.dueOn(today).filter { it.time != null }
    val done = items.count { it.doneOn(today) }

    Page(SolidColor(c.bg)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(1f)) {
                T(Fmt.dayFull(today), 14, FontWeight.SemiBold, c.muted)
                T("Расписание", 34, FontWeight.ExtraBold, letterSpacing = -0.5f)
            }
            Box(
                Modifier.height(36.dp).clip(RoundedCornerShape(18.dp)).background(c.card).padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) { T("$done из ${items.size}", 14, FontWeight.ExtraBold, c.accentDeep) }
        }

        if (items.isEmpty()) {
            Box(Modifier.fillMaxWidth().card(c.card, 24.dp).padding(24.dp)) {
                T("Укажи время у задач и привычек — и они появятся здесь по часам.", 15, FontWeight.SemiBold, c.muted)
            }
            NativeAdCard("schedule")
        }

        Column {
            items.forEachIndexed { i, t ->
                val isDone = t.doneOn(today)
                val last = i == items.lastIndex
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                    T(
                        t.time.orEmpty().removePrefix("0"), 13, FontWeight.Bold, c.muted,
                        Modifier.width(44.dp).padding(top = 15.dp), align = TextAlign.End
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.width(44.dp).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                        IconTile(t.icon, c.soft(t.hue), size = 44.dp, radius = 22.dp, iconSize = 22.dp)
                        Box(
                            Modifier.padding(top = 4.dp).width(3.dp).weight(1f).clip(RoundedCornerShape(2.dp))
                                .background(if (last) Color.Transparent else if (isDone) c.accent else c.track)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Row(
                        Modifier.weight(1f).padding(bottom = 10.dp)
                            .heightIn(min = (64 + (t.duration / 30).coerceAtMost(4) * 8).dp)
                            .card(c.card, 22.dp)
                            .clickable(onClickLabel = "Изменить", role = Role.Button) { nav.push(Screen.TaskEdit(t.id, today)) }
                            .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            T(
                                t.title, 16, FontWeight.Bold, if (isDone) c.muted else c.ink,
                                decoration = if (isDone) TextDecoration.LineThrough else null, maxLines = 2
                            )
                            T(Fmt.range(t.time, t.duration), 13, FontWeight.SemiBold, c.muted)
                        }
                        CheckCircle(isDone, if (isDone) "Вернуть в список" else "Отметить выполненным") { app.toggleTask(t.id, today) }
                    }
                }
                // Реклама — после второго дела, в видимой части экрана.
                if (i == minOf(1, items.lastIndex)) NativeAdCard("schedule", Modifier.padding(bottom = 10.dp))
            }
        }
    }
}
