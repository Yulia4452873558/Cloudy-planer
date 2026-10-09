package com.example.planer

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.planer.data.AppState
import com.example.planer.ui.components.PIcon
import com.example.planer.ui.screens.CalendarScreen
import com.example.planer.ui.screens.EventEditScreen
import com.example.planer.ui.screens.FocusScreen
import com.example.planer.ui.screens.HealthScreen
import com.example.planer.ui.screens.ProfileScreen
import com.example.planer.ui.screens.RewardsScreen
import com.example.planer.ui.screens.ScheduleScreen
import com.example.planer.ui.screens.TaskEditScreen
import com.example.planer.ui.screens.TodayScreen
import com.example.planer.ui.screens.WelcomeScreen
import com.example.planer.ui.theme.LocalP
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.YearMonth

enum class Tab(val label: String, val icon: String) {
    TODAY("Сегодня", "today"),
    CALENDAR("Календарь", "calendar"),
    SCHEDULE("Расписание", "clock"),
    HEALTH("Здоровье", "heart"),
    FOCUS("Фокус", "timer")
}

sealed interface Screen {
    data object Welcome : Screen
    data class Main(val tab: Tab) : Screen
    data object Rewards : Screen
    data object Profile : Screen
    data class TaskEdit(val id: Long?, val date: LocalDate) : Screen
    data class EventEdit(val id: Long?, val date: LocalDate) : Screen
}

class Nav(start: Screen) {
    var stack by mutableStateOf(listOf(start))
        private set

    /** Какой день открыт на «Сегодня» и какой месяц в календаре — не сбрасываются при переходах. */
    var todaySel: LocalDate by mutableStateOf(LocalDate.now())
    var calMonth: YearMonth by mutableStateOf(YearMonth.now())

    val current: Screen get() = stack.last()

    fun push(s: Screen) {
        stack = stack + s
    }

    fun pop() {
        if (stack.size > 1) stack = stack.dropLast(1)
    }

    fun tab(t: Tab) {
        stack = listOf(Screen.Main(t))
    }
}

@Composable
fun PlanerApp(app: AppState, nav: Nav) {
    val c = LocalP.current

    // Фокус-таймер тикает, на каком бы экране ни была пользовательница.
    LaunchedEffect(app.tRunning) {
        while (app.tRunning) {
            delay(250)
            app.tick()
        }
    }

    // Открыли приложение из уведомления таймера.
    LaunchedEffect(app.openFocusRequest) {
        if (app.openFocusRequest > 0 && app.data.onboarded) nav.tab(Tab.FOCUS)
    }

    BackHandler(enabled = nav.stack.size > 1) { nav.pop() }

    Box(Modifier.fillMaxSize().background(c.bg)) {
        Crossfade(targetState = nav.current, label = "screen") { s ->
            when (s) {
                Screen.Welcome -> WelcomeScreen(app, nav)
                is Screen.Main -> when (s.tab) {
                    Tab.TODAY -> TodayScreen(app, nav)
                    Tab.CALENDAR -> CalendarScreen(app, nav)
                    Tab.SCHEDULE -> ScheduleScreen(app, nav)
                    Tab.HEALTH -> HealthScreen(app, nav)
                    Tab.FOCUS -> FocusScreen(app)
                }
                Screen.Rewards -> RewardsScreen(app, nav)
                Screen.Profile -> ProfileScreen(app, nav)
                is Screen.TaskEdit -> TaskEditScreen(app, nav, s.id, s.date)
                is Screen.EventEdit -> EventEditScreen(app, nav, s.id, s.date)
            }
        }
        val cur = nav.current
        if (cur is Screen.Main) {
            GlassNavBar(
                current = cur.tab,
                onSelect = { nav.tab(it) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
            )
        }
    }
}

/** Полупрозрачная «стеклянная» панель с круглыми кнопками. */
@Composable
fun GlassNavBar(current: Tab, onSelect: (Tab) -> Unit, modifier: Modifier = Modifier) {
    val c = LocalP.current
    val shape = RoundedCornerShape(37.dp)
    val shadowColor = if (c.dark) Color.Black else Color(0xFF6F52C9)
    Row(
        modifier
            .fillMaxWidth()
            .height(74.dp)
            .shadow(16.dp, shape, ambientColor = shadowColor, spotColor = shadowColor)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(c.glassTop, c.glassBottom)))
            .border(1.dp, c.glassBorder, shape)
            .padding(horizontal = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Tab.entries.forEach { t ->
            val on = t == current
            var m = Modifier.size(56.dp)
            if (on) m = m.shadow(8.dp, CircleShape, ambientColor = shadowColor, spotColor = shadowColor)
            Box(
                m.clip(CircleShape)
                    .background(if (on) c.navOn else c.navOff)
                    .border(1.dp, if (on) Color.Transparent else c.glassBorder.copy(alpha = 0.5f), CircleShape)
                    .clickable(onClickLabel = t.label, role = Role.Tab) { onSelect(t) }
                    .semantics {
                        contentDescription = t.label
                        selected = on
                    },
                contentAlignment = Alignment.Center
            ) {
                PIcon(t.icon, if (on) c.navOnIcon else c.navOffIcon, 24.dp, stroke = 1.9f)
            }
        }
    }
}
