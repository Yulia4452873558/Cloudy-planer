package com.example.planer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.planer.Nav
import com.example.planer.data.AppData
import com.example.planer.data.AppState
import com.example.planer.data.Fmt
import com.example.planer.data.ThemeMode
import com.example.planer.data.streak
import com.example.planer.data.level
import com.example.planer.ui.components.BackHeader
import com.example.planer.ui.components.CapsTitle
import com.example.planer.ui.components.CircleButton
import com.example.planer.ui.components.Field
import com.example.planer.ui.components.Page
import com.example.planer.ui.components.Segmented
import com.example.planer.ui.components.SettingRow
import com.example.planer.ui.components.T
import com.example.planer.ui.components.Toggle
import com.example.planer.ui.components.card
import com.example.planer.ui.theme.LocalP
import java.time.LocalDate

@Composable
fun ProfileScreen(app: AppState, nav: Nav) {
    val c = LocalP.current
    val d = app.data
    val today = LocalDate.now()
    var name by remember { mutableStateOf(d.name) }
    var confirmReset by remember { mutableStateOf(false) }

    Page(c.pageBrush, bottom = 40.dp, gap = 16.dp) {
        BackHeader("Профиль", { nav.pop() })

        Row(
            Modifier.fillMaxWidth()
                .card(Brush.linearGradient(listOf(c.blue, c.lilac, c.pink)), 30.dp)
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(Modifier.width(72.dp).height(72.dp).card(c.card, 26.dp), contentAlignment = Alignment.Center) {
                T((name.firstOrNull() ?: 'Я').uppercase(), 30, FontWeight.ExtraBold, c.accentDeep)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Field(
                    name,
                    { v ->
                        name = v.take(24)
                        app.update { it.copy(name = name.trim()) }
                    },
                    "Твоё имя",
                    bg = c.tile,
                    size = 20,
                    minHeight = 44.dp
                )
                T("Серия ${Fmt.days(d.streak(today))} · уровень ${d.level()}", 14, FontWeight.SemiBold, c.muted)
            }
        }

        CapsTitle("НАПОМИНАНИЯ")
        Column(Modifier.fillMaxWidth().card(c.card, 26.dp).padding(horizontal = 16.dp)) {
            SettingRow("Утренний план", "Каждый день в 8:00") {
                Toggle(d.remindMorning, "Утренний план") { v -> app.update { it.copy(remindMorning = v) } }
            }
            SettingRow("Выпить воды", "С 10:00 до 20:00, каждые 2 часа") {
                Toggle(d.remindWater, "Выпить воды") { v -> app.update { it.copy(remindWater = v) } }
            }
            SettingRow("Вечерний итог", "В 21:00") {
                Toggle(d.remindEvening, "Вечерний итог") { v -> app.update { it.copy(remindEvening = v) } }
            }
            SettingRow("Прогноз цикла", "За 2 дня до начала", divider = false) {
                Toggle(d.remindCycle, "Прогноз цикла") { v -> app.update { it.copy(remindCycle = v) } }
            }
        }

        CapsTitle("ЦЕЛИ")
        Column(Modifier.fillMaxWidth().card(c.card, 26.dp).padding(horizontal = 16.dp)) {
            StepRow("Вода в день", Fmt.hours(d.waterGoal / 1000.0) + " л", true,
                { app.update { it.copy(waterGoal = (it.waterGoal - 250).coerceAtLeast(1000)) } },
                { app.update { it.copy(waterGoal = (it.waterGoal + 250).coerceAtMost(4000)) } })
            StepRow("Длина цикла", "${d.cycleLen} дн.", false,
                { app.update { it.copy(cycleLen = (it.cycleLen - 1).coerceAtLeast(21)) } },
                { app.update { it.copy(cycleLen = (it.cycleLen + 1).coerceAtMost(35)) } })
        }

        CapsTitle("ОФОРМЛЕНИЕ")
        Column(
            Modifier.fillMaxWidth().card(c.card, 26.dp).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            T("Тема", 16, FontWeight.Bold)
            val modes = ThemeMode.entries
            Segmented(modes.map { it.title }, modes.indexOf(d.theme), { i -> app.update { it.copy(theme = modes[i]) } })
            Row(Modifier.fillMaxWidth().height(44.dp), verticalAlignment = Alignment.CenterVertically) {
                T("Язык", 16, FontWeight.Bold, modifier = Modifier.weight(1f))
                T("Русский", 15, FontWeight.Bold, c.muted)
            }
        }

        Box(
            Modifier.fillMaxWidth().card(c.card, 26.dp)
                .clickable(role = Role.Button) {
                    if (confirmReset) {
                        app.update { AppData(onboarded = true, name = it.name, theme = it.theme, nextId = it.nextId, visits = setOf(today)) }
                        confirmReset = false
                    } else confirmReset = true
                }
                .padding(18.dp),
            contentAlignment = Alignment.Center
        ) {
            T(if (confirmReset) "Нажми ещё раз, чтобы удалить всё" else "Очистить все данные", 15, FontWeight.Bold, c.danger)
        }

        T("Планер · версия 1.0", 13, FontWeight.SemiBold, c.faint, Modifier.fillMaxWidth(), align = TextAlign.Center)
    }
}

@Composable
private fun StepRow(title: String, value: String, divider: Boolean, less: () -> Unit, more: () -> Unit) {
    val c = LocalP.current
    SettingRow(title, divider = divider) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircleButton("minus", "$title: меньше", less, bg = c.chip, size = 36.dp, iconSize = 16.dp)
            T(value, 15, FontWeight.ExtraBold, c.accentDeep, Modifier.width(72.dp), align = TextAlign.Center)
            CircleButton("plus", "$title: больше", more, bg = c.chip, size = 36.dp, iconSize = 16.dp)
        }
    }
}
