package com.example.planer.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.planer.data.Activity
import com.example.planer.data.Acts
import com.example.planer.data.AppState
import kotlinx.coroutines.delay
import com.example.planer.data.Music
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.LaunchedEffect
import com.example.planer.data.Fmt
import com.example.planer.ui.components.Bar
import com.example.planer.ui.components.CircleButton
import com.example.planer.ui.components.ErrorNote
import com.example.planer.ui.components.Field
import com.example.planer.ui.components.PIcon
import com.example.planer.ui.components.Pill
import com.example.planer.ui.components.PrimaryButton
import com.example.planer.ui.components.Segmented
import com.example.planer.ui.components.SettingRow
import com.example.planer.ui.components.Sheet
import com.example.planer.ui.components.T
import com.example.planer.ui.components.card
import com.example.planer.ui.theme.LocalP
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
fun FocusScreen(app: AppState) {
    val c = LocalP.current
    val d = app.data
    val today = LocalDate.now()
    var tipOpen by rememberSaveable { mutableStateOf(true) }
    var period by rememberSaveable { mutableIntStateOf(0) }
    // null — окно закрыто; Activity с пустым id — новое занятие.
    var editing by remember { mutableStateOf<Activity?>(null) }
    val cur = d.act(app.tAct)

    Box(Modifier.fillMaxSize().background(c.bg)) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).windowInsetsPadding(WindowInsets.navigationBars).padding(bottom = 130.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ---------- Таймер ----------
            Column(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                    .background(Brush.linearGradient(c.hero))
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(top = 16.dp, bottom = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                    T("Фокус", 34, FontWeight.ExtraBold, if (c.dark) c.ink else Color(0xFF2A2440), Modifier.weight(1f), letterSpacing = -0.5f)
                    CircleButton("reset", "Сбросить таймер", { app.resetTimer() }, bg = Color.White.copy(alpha = 0.6f), tint = Color(0xFF2A2440))
                }

                Box(Modifier.size(264.dp), contentAlignment = Alignment.Center) {
                    val progress = if (app.tTotal > 0) (app.tTotal - app.tLeft).toFloat() / app.tTotal else 0f
                    Box(Modifier.size(264.dp).border(1.5.dp, Color.White.copy(alpha = 0.7f), CircleShape))
                    Box(
                        Modifier.size(236.dp).shadow(24.dp, CircleShape, ambientColor = Color(0xFF6F52C9), spotColor = Color(0xFF6F52C9))
                            .clip(CircleShape).background(Color.White)
                    )
                    Canvas(Modifier.size(264.dp)) {
                        val sw = 10.dp.toPx()
                        val r = 104.dp.toPx()
                        val tl = Offset(center.x - r, center.y - r)
                        val sz = Size(r * 2, r * 2)
                        drawArc(Color(0xFFEEE9F8), 0f, 360f, false, tl, sz, style = Stroke(sw))
                        if (progress > 0f) {
                            drawArc(
                                Brush.sweepGradient(listOf(Color(0xFFA9D4F5), Color(0xFF8E74E0), Color(0xFFE6A6DA), Color(0xFFA9D4F5)), center),
                                -90f, 360f * progress, false, tl, sz, style = Stroke(sw, cap = StrokeCap.Round)
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        T(
                            if (app.tFinished) "Сессия завершена" else cur.title, 16, FontWeight.Bold, Color(0xFF5F5873),
                            Modifier.width(180.dp), align = TextAlign.Center, maxLines = 1
                        )
                        T(Fmt.mmss(app.tLeft), 52, FontWeight.ExtraBold, Color(0xFF2A2440), lineHeight = 56, letterSpacing = -1f)
                        Box(
                            Modifier.padding(top = 6.dp).size(56.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFFE5DBFA))
                                .clickable(onClickLabel = if (app.tRunning) "Пауза" else "Старт", role = Role.Button) { app.startPause() }
                                .semantics { contentDescription = if (app.tRunning) "Пауза" else "Старт" },
                            contentAlignment = Alignment.Center
                        ) { PIcon(if (app.tRunning) "pause" else "play", Color(0xFF6F52C9), 24.dp, filled = true) }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(10, 25, 45, 60).forEach { m ->
                        val on = app.tTotal == m * 60
                        Box(
                            Modifier.height(40.dp).width(72.dp).clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = if (on) 1f else 0.5f))
                                .clickable(role = Role.Button) { app.setDuration(m) },
                            contentAlignment = Alignment.Center
                        ) { T("$m мин", 14, FontWeight.Bold, Color(0xFF2A2440)) }
                    }
                }

                // Занятия: листаются вбок, последняя плитка — «Своё»
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    d.acts.forEach { a ->
                        val on = a.id == app.tAct
                        ActTile(a.emoji, a.title, on) { app.pickAct(a) }
                    }
                    ActTile("＋", "Своё", false) { editing = Activity("", "", "🎨", 30, 5, 0) }
                }
            }

            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // ---------- Цели недели ----------
                Row(Modifier.padding(start = 4.dp, top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    T("Цели недели", 18, FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                    // Кнопка открывает настройки выбранного сейчас занятия
                    Row(
                        Modifier.height(36.dp).clip(RoundedCornerShape(18.dp)).background(c.card)
                            .clickable(onClickLabel = "Изменить «${cur.title}»", role = Role.Button) { editing = cur }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PIcon("pen", c.accentDeep, 16.dp, stroke = 2f)
                        T("Изменить", 13, FontWeight.Bold, c.accentDeep)
                    }
                }
                T(
                    "Нажми на карточку занятия, чтобы поменять название, значок, длительность или цель.",
                    12, FontWeight.SemiBold, c.faint, Modifier.padding(horizontal = 4.dp), lineHeight = 16
                )
                val monday = today.with(DayOfWeek.MONDAY)
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 4.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    d.acts.forEach { a ->
                        var sec = 0
                        var dd = monday
                        while (!dd.isAfter(today)) {
                            sec += app.focusSeconds(dd, a.id)
                            dd = dd.plusDays(1)
                        }
                        val hours = sec / 3600.0
                        val pal = Acts.palettes[a.palette.coerceIn(0, Acts.palettes.size - 1)]
                        val fill = Color(pal.second)
                        Box(
                            Modifier.width(152.dp).height(176.dp)
                                .shadow(12.dp, RoundedCornerShape(32.dp), ambientColor = fill, spotColor = fill)
                                .clip(RoundedCornerShape(32.dp))
                                .background(Brush.linearGradient(pal.first.map { Color(it) }))
                                .background(
                                    Brush.linearGradient(
                                        0.45f to Color.White.copy(alpha = 0f),
                                        0.62f to Color.White.copy(alpha = 0.32f),
                                        0.78f to Color.White.copy(alpha = 0f)
                                    )
                                )
                                .clickable(onClickLabel = "Изменить занятие", role = Role.Button) { editing = a }
                                .padding(16.dp)
                        ) {
                            Box(
                                Modifier.size(48.dp).clip(RoundedCornerShape(17.dp)).background(Color.White.copy(alpha = 0.7f)),
                                contentAlignment = Alignment.Center
                            ) { T(a.emoji, 24, FontWeight.Normal, lineHeight = 28) }
                            Column(Modifier.align(Alignment.BottomStart), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                T(a.title, 18, FontWeight.Bold, Color.White, maxLines = 2, lineHeight = 21)
                                T("${Fmt.hours(hours)} из ${a.goalHours} ч", 13, FontWeight.SemiBold, Color.White)
                                Bar((hours / a.goalHours.coerceAtLeast(1)).toFloat(), 5.dp, Color.White.copy(alpha = 0.6f), Brush.linearGradient(listOf(fill, fill)), Modifier.padding(top = 6.dp))
                            }
                        }
                    }
                }

                // ---------- Совет ----------
                if (tipOpen) {
                    Row(
                        Modifier.fillMaxWidth().card(if (c.dark) c.card else Color(0xFFECEEFA), 28.dp)
                            .padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        T("🔔", 30, FontWeight.Normal, lineHeight = 34)
                        T(
                            "Выбери занятие, нажми ▶ — и время само попадёт в цели и статистику. Добавить своё занятие — плитка «Своё».",
                            13, FontWeight.SemiBold, c.muted, Modifier.weight(1f), lineHeight = 18
                        )
                        CircleButton("close", "Скрыть совет", { tipOpen = false }, bg = c.card, tint = c.muted, size = 36.dp, iconSize = 14.dp)
                    }
                }

                // ---------- Музыка ----------
                MusicCard()

                // ---------- Обзор ----------
                Column(
                    Modifier.fillMaxWidth().card(c.card, 28.dp).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    T("Обзор", 18, FontWeight.ExtraBold)
                    Segmented(listOf("Неделя", "Месяц"), period, { period = it })
                    val labels: List<String>
                    val values: List<Double>
                    if (period == 0) {
                        val days = (6 downTo 0).map { today.minusDays(it.toLong()) }
                        labels = days.map { Fmt.weekShort[it.dayOfWeek.value - 1] }
                        values = days.map { day -> app.focusSecondsAll(day) / 3600.0 }
                    } else {
                        labels = listOf("4 н", "3 н", "2 н", "Эта")
                        values = (3 downTo 0).map { w ->
                            var s = 0
                            for (i in 0..6) s += app.focusSecondsAll(today.minusDays((w * 7 + i).toLong()))
                            s / 3600.0
                        }
                    }
                    if (values.all { it < 0.01 }) {
                        Box(Modifier.fillMaxWidth().height(120.dp).card(c.chip, 20.dp).padding(16.dp), contentAlignment = Alignment.Center) {
                            T("Здесь появится график после первой фокус-сессии", 14, FontWeight.SemiBold, c.muted, align = TextAlign.Center)
                        }
                    } else {
                        Chart(labels, values)
                    }
                }

                // ---------- Сегодня ----------
                Column(
                    Modifier.fillMaxWidth().card(c.card, 28.dp).padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    T("Сегодня", 18, FontWeight.ExtraBold)
                    val rows = d.acts.map { it to app.focusSeconds(today, it.id) / 60 }.filter { it.second > 0 }
                    if (rows.isEmpty()) {
                        T("Пока пусто. Запусти таймер — и время появится здесь.", 14, FontWeight.SemiBold, c.muted)
                    }
                    val max = maxOf(60, rows.maxOfOrNull { it.second } ?: 0)
                    rows.forEach { (a, mins) ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                Modifier.size(48.dp).clip(RoundedCornerShape(17.dp))
                                    .background(
                                        if (c.dark) Brush.linearGradient(listOf(c.chip, c.chip))
                                        else Brush.linearGradient(listOf(Color(0xFFF8F5FE), Color(0xFFECEFFB)))
                                    ),
                                contentAlignment = Alignment.Center
                            ) { T(a.emoji, 22, FontWeight.Normal, lineHeight = 26) }
                            T(a.title, 15, FontWeight.Bold, modifier = Modifier.width(92.dp), maxLines = 1)
                            Bar(
                                mins.toFloat() / max, 6.dp, c.track,
                                Brush.horizontalGradient(listOf(Color(0xFFA9D4F5), Color(0xFF8E74E0))),
                                Modifier.weight(1f)
                            )
                            T(Fmt.minutes(mins), 14, FontWeight.ExtraBold, modifier = Modifier.width(78.dp), align = TextAlign.End, maxLines = 1)
                        }
                    }
                }
            }
        }

        val e = editing
        if (e != null) {
            ActivitySheet(
                start = e,
                canDelete = e.id.isNotEmpty() && d.acts.size > 1,
                onDismiss = { editing = null },
                onSave = {
                    app.saveActivity(it)
                    if (it.id.isNotEmpty() && it.id == app.tAct && !app.tRunning) app.pickAct(it)
                    editing = null
                },
                onDelete = {
                    app.deleteActivity(e.id)
                    editing = null
                }
            )
        }
    }
}

@Composable
private fun ActTile(emoji: String, title: String, on: Boolean, onClick: () -> Unit) {
    Column(
        Modifier.width(68.dp).clip(RoundedCornerShape(16.dp)).clickable(role = Role.RadioButton, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        var tile = Modifier.size(60.dp)
        if (on) tile = tile.shadow(10.dp, RoundedCornerShape(22.dp), ambientColor = Color(0xFF6F52C9), spotColor = Color(0xFF6F52C9))
        Box(
            tile.clip(RoundedCornerShape(22.dp))
                .background(
                    if (on) Brush.linearGradient(listOf(Color.White, Color(0xFFF6F2FF)))
                    else Brush.linearGradient(listOf(Color.White.copy(alpha = 0.5f), Color.White.copy(alpha = 0.22f)))
                )
                .border(1.dp, Color.White.copy(alpha = if (on) 0f else 0.35f), RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center
        ) { T(emoji, 28, FontWeight.Normal, Color(0xFF2A2440), lineHeight = 32) }
        T(title, 12, FontWeight.Bold, Color.White, align = TextAlign.Center, maxLines = 2, lineHeight = 14)
    }
}

/** Окно создания и изменения занятия. */
@Composable
private fun ActivitySheet(
    start: Activity,
    canDelete: Boolean,
    onDismiss: () -> Unit,
    onSave: (Activity) -> Unit,
    onDelete: () -> Unit
) {
    val c = LocalP.current
    var title by remember { mutableStateOf(start.title) }
    var emoji by remember { mutableStateOf(start.emoji) }
    var mins by remember { mutableIntStateOf(start.mins) }
    var goal by remember { mutableIntStateOf(start.goalHours) }
    var error by remember { mutableStateOf<String?>(null) }
    Sheet(if (start.id.isEmpty()) "Новое занятие" else "Занятие", onDismiss, footer = {
            ErrorNote(error)
            PrimaryButton("Сохранить", {
                val t = title.trim()
                if (t.isEmpty()) error = "Напиши название занятия"
                else onSave(start.copy(title = t, emoji = emoji, mins = mins, goalHours = goal))
            })
            if (canDelete) {
                Box(
                    Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(24.dp))
                        .clickable(role = Role.Button, onClick = onDelete),
                    contentAlignment = Alignment.Center
                ) { T("Удалить занятие", 15, FontWeight.Bold, c.danger) }
            }
    }) {
        Field(title, {
            title = it.take(28)
            error = null
        }, "Например, прогулка с собакой")
        T("Значок", 13, FontWeight.Bold, c.muted)
        Acts.emojis.chunked(6).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { em ->
                    Box(
                        Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(14.dp))
                            .background(if (em == emoji) c.lilac else c.chip)
                            .border(2.dp, if (em == emoji) c.accentDeep else Color.Transparent, RoundedCornerShape(14.dp))
                            .clickable(role = Role.RadioButton) { emoji = em },
                        contentAlignment = Alignment.Center
                    ) { T(em, 22, FontWeight.Normal, lineHeight = 26) }
                }
                repeat(6 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
        T("Длительность сессии", 13, FontWeight.Bold, c.muted)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(5, 10, 15, 20, 25, 30, 45, 60, 90).forEach { m ->
                Pill("$m мин", mins == m, { mins = m }, height = 40.dp, bg = c.chip, selectedBg = c.accent)
            }
        }
        SettingRow("Цель в неделю", divider = false) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircleButton("minus", "Меньше", { goal = (goal - 1).coerceAtLeast(1) }, bg = c.chip, size = 36.dp, iconSize = 16.dp)
                T("$goal ч", 15, FontWeight.ExtraBold, c.accentDeep, Modifier.width(56.dp), align = TextAlign.Center)
                CircleButton("plus", "Больше", { goal = (goal + 1).coerceAtMost(60) }, bg = c.chip, size = 36.dp, iconSize = 16.dp)
            }
        }
    }
}

/** Плавный график с подсказкой над последней точкой. */
@Composable
private fun Chart(labels: List<String>, values: List<Double>) {
    val c = LocalP.current
    val hi = values.lastIndex
    BoxWithConstraints(Modifier.fillMaxWidth().height(150.dp)) {
        val wDp = maxWidth.value
        val hDp = 150f
        val max = (values.maxOrNull() ?: 0.0).coerceAtLeast(0.5) * 1.15
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val padX = 16.dp.toPx()
            val n = values.size
            val pts = values.mapIndexed { i, v ->
                Offset(
                    padX + i * (w - padX * 2) / (n - 1).coerceAtLeast(1),
                    h - 10.dp.toPx() - (v / max).toFloat() * (h - 50.dp.toPx())
                )
            }
            val hp = pts[hi]
            drawRoundRect(
                c.accent.copy(alpha = 0.1f),
                topLeft = Offset(hp.x - 14.dp.toPx(), 30.dp.toPx()),
                size = Size(28.dp.toPx(), h - 30.dp.toPx()),
                cornerRadius = CornerRadius(14.dp.toPx())
            )
            val line = Path().apply {
                moveTo(pts[0].x, pts[0].y)
                for (i in 1 until n) {
                    val a = pts[i - 1]
                    val b = pts[i]
                    val dx = (b.x - a.x) / 2
                    cubicTo(a.x + dx, a.y, b.x - dx, b.y, b.x, b.y)
                }
            }
            val area = Path().apply {
                addPath(line)
                lineTo(pts[n - 1].x, h)
                lineTo(pts[0].x, h)
                close()
            }
            drawPath(area, Color(0xFFA9D4F5).copy(alpha = 0.28f))
            drawPath(line, c.accent, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawCircle(Color.White, 6.dp.toPx(), hp)
            drawCircle(c.accent, 6.dp.toPx(), hp, style = Stroke(3.dp.toPx()))
        }
        val n = values.size
        val xDp = 16f + hi * (wDp - 32f) / (n - 1).coerceAtLeast(1)
        val yDp = hDp - 10f - (values[hi] / max).toFloat() * (hDp - 50f)
        Box(
            Modifier.offset(x = (xDp - 30f).coerceIn(0f, (wDp - 60f).coerceAtLeast(0f)).dp, y = (yDp - 42f).coerceAtLeast(0f).dp)
                .width(60.dp).height(28.dp).clip(RoundedCornerShape(14.dp)).background(c.ink),
            contentAlignment = Alignment.Center
        ) { T(Fmt.hours(values[hi]) + " ч", 13, FontWeight.ExtraBold, c.bg) }
    }
    Row(Modifier.fillMaxWidth().padding(horizontal = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        labels.forEachIndexed { i, l ->
            T(
                l, 12, if (i == hi) FontWeight.ExtraBold else FontWeight.SemiBold,
                if (i == hi) c.ink else c.faint, Modifier.width(28.dp), align = TextAlign.Center, maxLines = 1
            )
        }
    }
}

/** Музыка: играет и при открытом приложении, и в фоне при выключенном экране. */
@Composable
private fun MusicCard() {
    val c = LocalP.current
    val tracks = Music.tracks
    if (tracks.isEmpty()) return
    LaunchedEffect(Music.playing) {
        while (Music.playing) {
            Music.sync()
            delay(500)
        }
    }
    val cur = tracks.getOrNull(Music.index)
    val dur = Music.duration
    Column(
        Modifier.fillMaxWidth().card(c.card, 28.dp).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            T("Музыка", 18, FontWeight.ExtraBold, modifier = Modifier.weight(1f))
            T("играет и с выключенным экраном", 12, FontWeight.SemiBold, c.faint)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                Modifier.size(56.dp).clip(RoundedCornerShape(20.dp)).background(Brush.linearGradient(c.hero)),
                contentAlignment = Alignment.Center
            ) { T("🎧", 26, FontWeight.Normal, lineHeight = 30) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                T(cur?.title ?: "Выбери трек", 16, FontWeight.ExtraBold, maxLines = 1)
                Box(
                    Modifier.fillMaxWidth().height(20.dp)
                        .pointerInput(dur) { detectTapGestures { o -> Music.seekTo(o.x / size.width) } },
                    contentAlignment = Alignment.Center
                ) {
                    Bar(
                        if (dur > 0) Music.position.toFloat() / dur else 0f, 5.dp, c.track,
                        Brush.horizontalGradient(listOf(Color(0xFFA9D4F5), Color(0xFF8E74E0)))
                    )
                }
                Row {
                    T(Fmt.mmss((Music.position / 1000).toInt()), 12, FontWeight.SemiBold, c.muted, Modifier.weight(1f))
                    T(Fmt.mmss((dur / 1000).toInt()), 12, FontWeight.SemiBold, c.muted)
                }
            }
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleButton("prev", "Предыдущий трек", { Music.prev() }, bg = c.chip, size = 48.dp, iconSize = 20.dp)
            Box(
                Modifier.size(62.dp).clip(CircleShape).background(c.accentDeep)
                    .clickable(onClickLabel = if (Music.playing) "Пауза" else "Играть", role = Role.Button) { Music.toggle() }
                    .semantics { contentDescription = if (Music.playing) "Пауза" else "Играть" },
                contentAlignment = Alignment.Center
            ) { PIcon(if (Music.playing) "pause" else "play", c.onAccent, 26.dp, filled = true) }
            CircleButton("next", "Следующий трек", { Music.next() }, bg = c.chip, size = 48.dp, iconSize = 20.dp)
        }
        tracks.forEachIndexed { i, t ->
            val on = i == Music.index
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                    .background(if (on) c.chip else Color.Transparent)
                    .clickable(onClickLabel = t.title, role = Role.Button) { if (on) Music.toggle() else Music.play(i) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                    PIcon(if (on && Music.playing) "pause" else "play", if (on) c.accentDeep else c.faint, 16.dp, filled = true)
                }
                T(t.title, 15, if (on) FontWeight.ExtraBold else FontWeight.Bold, if (on) c.accentDeep else c.ink, Modifier.weight(1f), maxLines = 1)
                if (t.durationMs > 0) T(Fmt.mmss((t.durationMs / 1000).toInt()), 13, FontWeight.SemiBold, c.muted)
            }
        }
    }
}
