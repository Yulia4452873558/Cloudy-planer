package com.example.planer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.planer.Nav
import com.example.planer.data.AppState
import com.example.planer.data.Fmt
import com.example.planer.data.level
import com.example.planer.data.points
import com.example.planer.data.record
import com.example.planer.data.streak
import com.example.planer.data.totalDone
import com.example.planer.data.waterGoalDays
import com.example.planer.ui.components.Bar
import com.example.planer.ui.components.BackHeader
import com.example.planer.ui.components.IconTile
import com.example.planer.ui.components.Mascot
import com.example.planer.ui.components.Page
import com.example.planer.ui.components.T
import com.example.planer.ui.components.card
import com.example.planer.ui.theme.LocalP
import java.time.LocalDate

/** Значок: что нужно сделать, сколько уже сделано и сколько нужно. */
private data class Badge(
    val title: String,
    val how: String,
    val icon: String,
    val have: Int,
    val need: Int,
    val bg: Color
) {
    val earned: Boolean get() = have >= need
}

@Composable
fun RewardsScreen(app: AppState, nav: Nav) {
    val c = LocalP.current
    val d = app.data
    val today = LocalDate.now()
    val streak = d.streak(today)
    val record = d.record(today)
    val points = d.points()
    val level = d.level()
    val inLevel = points % 200
    val done = d.totalDone()
    val focusDays = d.focus.count { (_, m) -> m.values.sum() > 0 }

    val badges = listOf(
        Badge("Первый шаг", "Выполни первую задачу", "check", done, 1, c.mint),
        Badge("Три дня подряд", "Открывай планер 3 дня подряд", "flame", record, 3, c.peach),
        Badge("Водный баланс", "Выпей дневную норму воды", "drop", d.waterGoalDays(), 1, c.blue),
        Badge("Первый фокус", "Заверши фокус-сессию до конца", "clock", d.sessions, 1, c.lilac),
        Badge("Неделя с Облачком", "7 дней подряд в планере", "star", record, 7, c.lemon),
        Badge("Десять дел", "Выполни 10 задач или привычек", "today", done, 10, c.pink),
        Badge("Мастер фокуса", "Заверши 10 фокус-сессий", "timer", d.sessions, 10, c.lilac),
        Badge("Фокус-неделя", "Занимайся с таймером 7 разных дней", "pulse", focusDays, 7, c.mint),
        Badge("Месяц подряд", "30 дней подряд в планере", "heart", record, 30, c.pink)
    )
    val earned = badges.count { it.earned }

    Page(c.pageBrush, bottom = 40.dp, gap = 16.dp) {
        BackHeader("Награды", { nav.pop() })

        Row(
            Modifier.fillMaxWidth().card(c.peach, 28.dp).padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            IconTile("flame", c.tile, size = 84.dp, radius = 28.dp, iconSize = 44.dp)
            Column {
                T(Fmt.days(streak), 40, FontWeight.ExtraBold, letterSpacing = -1f, lineHeight = 44)
                T("подряд без пропусков", 16, FontWeight.Bold)
                T("Личный рекорд — " + Fmt.days(record), 13, FontWeight.SemiBold, c.muted)
            }
        }
        T(
            "Серия растёт, если открывать планер каждый день. Пропустишь день — начнётся заново, а рекорд сохранится.",
            13, FontWeight.SemiBold, c.muted, Modifier.padding(horizontal = 4.dp), lineHeight = 18
        )

        Row(
            Modifier.fillMaxWidth().card(c.card, 28.dp).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Mascot()
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                T("Облачко · уровень $level", 16, FontWeight.ExtraBold)
                Bar(inLevel / 200f, 10.dp, c.track, SolidColor(c.accent))
                T("$inLevel из 200 очков до уровня ${level + 1}", 13, FontWeight.SemiBold, c.muted)
            }
        }
        T(
            "Очки: задача — 5, фокус-сессия — 20, норма воды — 10, каждый новый день в планере — 5.",
            13, FontWeight.SemiBold, c.muted, Modifier.padding(horizontal = 4.dp), lineHeight = 18
        )

        Row(Modifier.padding(start = 4.dp, top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            T("Значки", 18, FontWeight.ExtraBold, modifier = Modifier.weight(1f))
            T("$earned из ${badges.size}", 14, FontWeight.ExtraBold, c.accentDeep)
        }
        badges.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { b ->
                    Column(
                        Modifier.weight(1f).card(if (b.earned) b.bg else c.card, 24.dp).padding(vertical = 14.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconTile(
                            b.icon, if (b.earned) c.tile else c.chip, size = 52.dp, radius = 18.dp, iconSize = 26.dp,
                            tint = if (b.earned) c.ink else c.faint
                        )
                        T(b.title, 13, FontWeight.ExtraBold, align = TextAlign.Center, lineHeight = 16, maxLines = 2)
                        T(b.how, 11, FontWeight.SemiBold, c.muted, align = TextAlign.Center, lineHeight = 14, maxLines = 3)
                        if (b.earned) {
                            T("Получен", 11, FontWeight.ExtraBold, c.accentDeep)
                        } else {
                            Bar(b.have.toFloat() / b.need, 5.dp, c.track, SolidColor(c.accent), Modifier.padding(horizontal = 6.dp))
                            T("${minOf(b.have, b.need)} из ${b.need}", 11, FontWeight.Bold, c.muted)
                        }
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}
