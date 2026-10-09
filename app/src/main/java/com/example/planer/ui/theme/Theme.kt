package com.example.planer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.planer.R
import com.example.planer.data.EventType
import com.example.planer.data.Hue

val Manrope = FontFamily(
    Font(R.font.manrope_medium, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold)
)

data class PColors(
    val dark: Boolean,
    val bg: Color,
    val bgTop: Color,
    val bgMid: Color,
    val card: Color,
    val ink: Color,
    val muted: Color,
    val faint: Color,
    val accent: Color,
    val accentDeep: Color,
    val onAccent: Color,
    val track: Color,
    val chip: Color,
    val line: Color,
    val tile: Color,
    val pink: Color,
    val peach: Color,
    val mint: Color,
    val blue: Color,
    val lilac: Color,
    val lemon: Color,
    val glassTop: Color,
    val glassBottom: Color,
    val glassBorder: Color,
    val navOn: Color,
    val navOnIcon: Color,
    val navOff: Color,
    val navOffIcon: Color,
    val danger: Color,
    val hero: List<Color>
) {
    fun soft(h: Hue): Color = when (h) {
        Hue.PINK -> pink
        Hue.PEACH -> peach
        Hue.MINT -> mint
        Hue.BLUE -> blue
        Hue.LILAC -> lilac
        Hue.LEMON -> lemon
    }

    fun typeLine(t: EventType): Color = when (t) {
        EventType.BIRTHDAY -> Color(0xFF8E74E0)
        EventType.HOLIDAY -> Color(0xFFF79A4B)
        EventType.OTHER -> Color(0xFF5B9BEF)
    }

    fun typeText(t: EventType): Color = if (dark) {
        when (t) {
            EventType.BIRTHDAY -> Color(0xFFC9B8FF)
            EventType.HOLIDAY -> Color(0xFFFFC08A)
            EventType.OTHER -> Color(0xFF9EC4FF)
        }
    } else {
        when (t) {
            EventType.BIRTHDAY -> Color(0xFF6F52C9)
            EventType.HOLIDAY -> Color(0xFFB4560F)
            EventType.OTHER -> Color(0xFF2F5CA8)
        }
    }

    val pageBrush: Brush get() = Brush.verticalGradient(listOf(bgTop, bgMid, bg))
}

val LightColors = PColors(
    dark = false,
    bg = Color(0xFFF5F2FA),
    bgTop = Color(0xFFF3EEFC),
    bgMid = Color(0xFFF8F1F8),
    card = Color.White,
    ink = Color(0xFF2A2440),
    muted = Color(0xFF5F5873),
    faint = Color(0xFF8C85A3),
    accent = Color(0xFF8E74E0),
    accentDeep = Color(0xFF6F52C9),
    onAccent = Color.White,
    track = Color(0xFFEBE5F7),
    chip = Color(0xFFF3EEFC),
    line = Color(0xFFF0ECF7),
    tile = Color.White.copy(alpha = 0.72f),
    pink = Color(0xFFFAD7EC),
    peach = Color(0xFFFCE3C8),
    mint = Color(0xFFDCF2C6),
    blue = Color(0xFFD5E5FB),
    lilac = Color(0xFFE5DBFA),
    lemon = Color(0xFFFBF0BF),
    glassTop = Color.White.copy(alpha = 0.62f),
    glassBottom = Color.White.copy(alpha = 0.34f),
    glassBorder = Color.White.copy(alpha = 0.8f),
    navOn = Color.White,
    navOnIcon = Color(0xFF2A2440),
    navOff = Color.White.copy(alpha = 0.4f),
    navOffIcon = Color(0xFF6B6480),
    danger = Color(0xFFB4415F),
    hero = listOf(Color(0xFFA9D4F5), Color(0xFFC7B8F2), Color(0xFFF2C4E4))
)

val DarkColors = PColors(
    dark = true,
    bg = Color(0xFF17141F),
    bgTop = Color(0xFF1D1828),
    bgMid = Color(0xFF1A1623),
    card = Color(0xFF241F31),
    ink = Color(0xFFF2EEFA),
    muted = Color(0xFFB3ACC8),
    faint = Color(0xFF8C85A3),
    accent = Color(0xFF9C84EE),
    accentDeep = Color(0xFF9C84EE),
    onAccent = Color(0xFF17141F),
    track = Color(0xFF342D45),
    chip = Color(0xFF2C2639),
    line = Color(0xFF2E2840),
    tile = Color.White.copy(alpha = 0.08f),
    pink = Color(0xFF3A2A38),
    peach = Color(0xFF3A2F2A),
    mint = Color(0xFF2A3A2C),
    blue = Color(0xFF26303F),
    lilac = Color(0xFF2F2A45),
    lemon = Color(0xFF38342A),
    glassTop = Color(0xFF3C3450).copy(alpha = 0.62f),
    glassBottom = Color(0xFF241F32).copy(alpha = 0.5f),
    glassBorder = Color.White.copy(alpha = 0.14f),
    navOn = Color(0xFFF2EEFA),
    navOnIcon = Color(0xFF17141F),
    navOff = Color.White.copy(alpha = 0.08f),
    navOffIcon = Color(0xFFC9C2DA),
    danger = Color(0xFFFF8FA8),
    hero = listOf(Color(0xFF2B3A55), Color(0xFF3A2F5A), Color(0xFF4A2E4A))
)

val LocalP = staticCompositionLocalOf { LightColors }

@Composable
fun PlanerTheme(dark: Boolean, content: @Composable () -> Unit) {
    val colors = if (dark) DarkColors else LightColors
    CompositionLocalProvider(LocalP provides colors) {
        MaterialTheme(
            colorScheme = if (dark) darkColorScheme(primary = colors.accent) else lightColorScheme(primary = colors.accentDeep),
            content = content
        )
    }
}
