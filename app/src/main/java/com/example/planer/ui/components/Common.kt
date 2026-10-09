package com.example.planer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import com.example.planer.ui.theme.LocalP
import com.example.planer.ui.theme.Manrope

/** Текст в фирменном шрифте. */
@Composable
fun T(
    text: String,
    size: Int,
    weight: FontWeight = FontWeight.Bold,
    color: Color = LocalP.current.ink,
    modifier: Modifier = Modifier,
    align: TextAlign? = null,
    decoration: TextDecoration? = null,
    maxLines: Int = Int.MAX_VALUE,
    lineHeight: Int? = null,
    letterSpacing: Float = 0f
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = size.sp,
        fontWeight = weight,
        fontFamily = Manrope,
        textAlign = align,
        textDecoration = decoration,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        lineHeight = (lineHeight ?: (size * 1.3f).toInt()).sp,
        letterSpacing = letterSpacing.sp
    )
}

/** Прокручиваемая страница с отступом под статус-бар и навигацию. */
@Composable
fun Page(
    background: Brush,
    bottom: Dp = 130.dp,
    gap: Dp = 18.dp,
    /** Высота закреплённой снизу кнопки. Если задана, область прокрутки
     *  заканчивается над кнопкой и над клавиатурой — поле ввода не перекрывается. */
    bottomBar: Dp? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(Modifier.fillMaxSize().background(background)) {
        var viewport = Modifier.fillMaxSize()
        if (bottomBar != null) {
            viewport = viewport
                .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                .padding(bottom = bottomBar)
        }
        Column(
            viewport
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = bottom),
            verticalArrangement = Arrangement.spacedBy(gap),
            content = content
        )
    }
}

fun Modifier.card(color: Color, radius: Dp = 28.dp): Modifier =
    this.clip(RoundedCornerShape(radius)).background(color)

fun Modifier.card(brush: Brush, radius: Dp = 28.dp): Modifier =
    this.clip(RoundedCornerShape(radius)).background(brush)

/** Блик по диагонали, как на стеклянных карточках. */
fun gloss(base: List<Color>): Brush = Brush.linearGradient(base)

@Composable
fun IconTile(
    icon: String,
    bg: Color,
    size: Dp = 48.dp,
    radius: Dp = 16.dp,
    iconSize: Dp = 24.dp,
    tint: Color = LocalP.current.ink
) {
    Box(Modifier.size(size).clip(RoundedCornerShape(radius)).background(bg), contentAlignment = Alignment.Center) {
        PIcon(icon, tint, iconSize)
    }
}

@Composable
fun CircleButton(
    icon: String,
    label: String,
    onClick: () -> Unit,
    bg: Color = LocalP.current.card,
    tint: Color = LocalP.current.ink,
    size: Dp = 44.dp,
    iconSize: Dp = 20.dp
) {
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(bg)
            .clickable(onClickLabel = label, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) { PIcon(icon, tint, iconSize, stroke = 2.2f) }
}

@Composable
fun CheckCircle(done: Boolean, label: String, onClick: () -> Unit) {
    val c = LocalP.current
    Box(
        Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClickLabel = label, role = Role.Checkbox, onClick = onClick)
            .semantics { stateDescription = if (done) "выполнено" else "не выполнено" },
        contentAlignment = Alignment.Center
    ) {
        if (done) {
            Box(
                Modifier.size(30.dp).shadow(6.dp, CircleShape, ambientColor = c.accent, spotColor = c.accent)
                    .clip(CircleShape).background(c.accent),
                contentAlignment = Alignment.Center
            ) { PIcon("check", c.onAccent, 18.dp, stroke = 2.8f) }
        } else {
            Box(
                Modifier.size(30.dp).clip(CircleShape)
                    .background(if (c.dark) Color.Transparent else Color.White.copy(alpha = 0.7f))
                    .border(2.dp, c.ink.copy(alpha = 0.45f), CircleShape)
            )
        }
    }
}

@Composable
fun Pill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    height: Dp = 44.dp,
    selectedBg: Color = LocalP.current.accentDeep,
    bg: Color = LocalP.current.card,
    border: Color? = null
) {
    val c = LocalP.current
    var m = Modifier
        .height(height)
        .clip(RoundedCornerShape(height / 2))
        .background(if (selected) selectedBg else bg)
    if (border != null) m = m.border(2.dp, border, RoundedCornerShape(height / 2))
    Box(
        m.clickable(role = Role.Button, onClick = onClick).padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        T(text, 15, FontWeight.Bold, if (selected && border == null) c.onAccent else c.ink)
    }
}

/** Сегментированный переключатель в стиле iOS. */
@Composable
fun Segmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val c = LocalP.current
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(c.chip).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { i, o ->
            val on = i == selected
            Box(
                Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(18.dp))
                    .background(if (on) c.card else Color.Transparent)
                    .clickable(role = Role.Tab) { onSelect(i) },
                contentAlignment = Alignment.Center
            ) { T(o, 14, FontWeight.Bold, if (on) c.ink else c.muted) }
        }
    }
}

@Composable
fun Toggle(on: Boolean, label: String, onChange: (Boolean) -> Unit) {
    val c = LocalP.current
    Box(
        Modifier
            .width(52.dp).height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (on) Brush.horizontalGradient(listOf(Color(0xFFB9A6EE), Color(0xFF8E74E0)))
                else SolidColor(if (c.dark) Color(0xFF3A3350) else Color(0xFFE4DFEE))
            )
            .clickable(onClickLabel = label, role = Role.Switch) { onChange(!on) }
            .semantics { stateDescription = if (on) "включено" else "выключено" }
            .padding(2.dp),
        contentAlignment = if (on) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(Modifier.size(28.dp).shadow(3.dp, CircleShape).clip(CircleShape).background(Color.White))
    }
}

@Composable
fun Bar(fraction: Float, height: Dp, track: Color, fill: Brush, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(height).clip(RoundedCornerShape(height / 2)).background(track)) {
        Box(
            Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).height(height)
                .clip(RoundedCornerShape(height / 2)).background(fill)
        )
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = LocalP.current
    Box(
        modifier.fillMaxWidth().height(58.dp)
            .shadow(14.dp, RoundedCornerShape(29.dp), ambientColor = c.accentDeep, spotColor = c.accentDeep)
            .clip(RoundedCornerShape(29.dp)).background(c.accentDeep)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { T(text, 17, FontWeight.ExtraBold, c.onAccent) }
}

@Composable
fun Fab(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = LocalP.current
    Box(
        modifier.size(58.dp)
            .shadow(14.dp, CircleShape, ambientColor = c.accentDeep, spotColor = c.accentDeep)
            .clip(CircleShape).background(c.accentDeep)
            .clickable(onClickLabel = label, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) { PIcon("plus", c.onAccent, 26.dp, stroke = 2.6f) }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    T(text, 18, FontWeight.ExtraBold, modifier = modifier.padding(horizontal = 4.dp))
}

@Composable
fun CapsTitle(text: String) {
    T(text, 13, FontWeight.ExtraBold, LocalP.current.muted, Modifier.padding(start = 4.dp, top = 4.dp), letterSpacing = 1.5f)
}

/** Заголовок экрана с кнопкой «назад». */
@Composable
fun BackHeader(title: String, onBack: () -> Unit, trailing: @Composable RowScope.() -> Unit = {}) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CircleButton("back", "Назад", onBack)
        T(title, 28, FontWeight.ExtraBold, modifier = Modifier.weight(1f), maxLines = 1)
        trailing()
    }
}

/** Поле ввода в мягкой плашке. */
@Composable
fun Field(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    bg: Color = LocalP.current.chip,
    size: Int = 17,
    minHeight: Dp = 52.dp
) {
    val c = LocalP.current
    BasicTextField(
        value = value,
        onValueChange = onChange,
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        textStyle = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = size.sp, color = c.ink),
        cursorBrush = SolidColor(c.accentDeep),
        modifier = modifier,
        decorationBox = { inner ->
            Box(
                Modifier.fillMaxWidth().then(Modifier.height(if (singleLine) minHeight else 96.dp))
                    .clip(RoundedCornerShape(18.dp)).background(bg)
                    .padding(PaddingValues(horizontal = 16.dp, vertical = if (singleLine) 0.dp else 12.dp)),
                contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart
            ) {
                if (value.isEmpty()) T(placeholder, size, FontWeight.SemiBold, c.faint)
                inner()
            }
        }
    )
}

/** Строка настроек: подпись слева, содержимое справа. */
@Composable
fun SettingRow(title: String, note: String? = null, divider: Boolean = true, trailing: @Composable () -> Unit) {
    val c = LocalP.current
    Column {
        Row(
            Modifier.fillMaxWidth().height(if (note == null) 56.dp else 64.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                T(title, 16, FontWeight.Bold)
                if (note != null) T(note, 13, FontWeight.SemiBold, c.muted)
            }
            trailing()
        }
        if (divider) Box(Modifier.fillMaxWidth().height(1.dp).background(c.line))
    }
}

/** Облачко-маскот. [mouth] — контур рта в координатах 40×24. */
@Composable
fun Mascot(mouth: String = "M15 15c2 3 8 3 10 0", k: Float = 1f, modifier: Modifier = Modifier) {
    val mouthPath = remember(mouth) { PathParser().parsePathString(mouth).toPath() }
    val eyes = remember { PathParser().parsePathString("M12 8v.01M28 8v.01").toPath() }
    Canvas(modifier.width((92 * k).dp).height((66 * k).dp)) {
        val u = size.width / 92f
        drawOval(Color(0x2E6F52C9), topLeft = Offset(12 * u, 58 * u), size = Size(68 * u, 8 * u))
        drawCircle(Color.White, 18 * u, Offset(22 * u, 42 * u))
        drawCircle(Color.White, 24 * u, Offset(46 * u, 26 * u))
        drawCircle(Color.White, 18 * u, Offset(70 * u, 38 * u))
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(Color.White, Color.White, Color(0xFFE9E0FB)), startY = 34 * u, endY = 64 * u),
            topLeft = Offset(6 * u, 34 * u),
            size = Size(80 * u, 30 * u),
            cornerRadius = CornerRadius(15 * u)
        )
        drawRoundRect(color = Color(0xFFFFB7E0), topLeft = Offset(20 * u, 40 * u), size = Size(10 * u, 6 * u), cornerRadius = CornerRadius(5 * u))
        drawRoundRect(color = Color(0xFFFFB7E0), topLeft = Offset(62 * u, 40 * u), size = Size(10 * u, 6 * u), cornerRadius = CornerRadius(5 * u))
        translate(26 * u, 26 * u) {
            scale(u, u, pivot = Offset.Zero) {
                val st = Stroke(width = 2.2f, cap = StrokeCap.Round)
                drawPath(eyes, Color(0xFF2A2440), style = st)
                drawPath(mouthPath, Color(0xFF2A2440), style = st)
            }
        }
    }
}

/** Карточка, которую можно смахнуть влево, чтобы удалить. */
@Composable
fun SwipeToDelete(onDelete: () -> Unit, radius: Dp = 24.dp, content: @Composable () -> Unit) {
    val c = LocalP.current
    val scope = rememberCoroutineScope()
    val offset = remember { Animatable(0f) }
    val currentOnDelete by rememberUpdatedState(onDelete)
    var width by remember { mutableIntStateOf(1) }
    Box(
        Modifier.fillMaxWidth()
            .onSizeChanged { width = it.width.coerceAtLeast(1) }
            .clip(RoundedCornerShape(radius))
            .background(c.danger.copy(alpha = if (c.dark) 0.3f else 0.16f))
    ) {
        Row(
            Modifier.matchParentSize().padding(end = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            T("Удалить", 15, FontWeight.Bold, c.danger)
            PIcon("trash", c.danger, 22.dp, stroke = 2f)
        }
        Box(
            Modifier
                .offset { IntOffset(offset.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                if (offset.value < -width * 0.35f) {
                                    offset.animateTo(-width.toFloat())
                                    currentOnDelete()
                                    offset.snapTo(0f)
                                } else {
                                    offset.animateTo(0f)
                                }
                            }
                        },
                        onDragCancel = { scope.launch { offset.animateTo(0f) } },
                        onHorizontalDrag = { change, drag ->
                            change.consume()
                            scope.launch { offset.snapTo((offset.value + drag).coerceIn(-width.toFloat(), 0f)) }
                        }
                    )
                }
        ) { content() }
    }
}

/** Всплывающее окно в стиле приложения. */
@Composable
fun Sheet(
    title: String,
    onDismiss: () -> Unit,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val c = LocalP.current
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().imePadding().clip(RoundedCornerShape(30.dp)).background(c.card).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                T(title, 20, FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                CircleButton("close", "Закрыть", onDismiss, bg = c.chip, size = 40.dp, iconSize = 16.dp)
            }
            // Содержимое прокручивается, а кнопки внизу всегда видны.
            Column(
                Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                content = content
            )
            if (footer != null) footer()
        }
    }
}

/** Заметная подсказка об ошибке (например, не заполнено название). */
@Composable
fun ErrorNote(text: String?, modifier: Modifier = Modifier) {
    if (text == null) return
    val c = LocalP.current
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(c.danger.copy(alpha = if (c.dark) 0.25f else 0.12f))
            .semantics { liveRegion = LiveRegionMode.Polite }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PIcon("alert", c.danger, 18.dp, stroke = 2f)
        T(text, 14, FontWeight.Bold, c.danger)
    }
}
