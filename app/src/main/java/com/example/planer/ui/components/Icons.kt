package com.example.planer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Линейные иконки 24×24 — те же контуры, что в прототипе. */
object Ic {
    private val paths = mapOf(
        "today" to "M4 6l1.5 1.5L8 5M4 12l1.5 1.5L8 11M4 18l1.5 1.5L8 17M11 6h9M11 12h9M11 18h9",
        "calendar" to "M4 6h16v14H4zM4 10h16M8 3v4M16 3v4",
        "clock" to "M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18zM12 7v5l3 2",
        "heart" to "M12 20s-7-4.5-7-10a4 4 0 0 1 7-2.5A4 4 0 0 1 19 10c0 5.5-7 10-7 10z",
        "timer" to "M12 5a8 8 0 1 0 0 16 8 8 0 0 0 0-16zM12 9v4l2.5 2M9.5 2h5",
        "cup" to "M5 9h11v5a5 5 0 0 1-5 5h-1a5 5 0 0 1-5-5V9zM16 10h1.5a2.5 2.5 0 0 1 0 5H16M8 3v3M12 3v3",
        "drop" to "M12 3c3.5 4 6 7 6 10.5a6 6 0 0 1-12 0C6 10 8.5 7 12 3z",
        "sparkle" to "M12 3l1.8 5.2L19 10l-5.2 1.8L12 17l-1.8-5.2L5 10l5.2-1.8L12 3zM19 16v4M17 18h4",
        "book" to "M4 4.5A1.5 1.5 0 0 1 5.5 3H20v14H5.5A1.5 1.5 0 0 0 4 18.5v-14zM4 18.5A1.5 1.5 0 0 0 5.5 20H20",
        "pulse" to "M3 12h3l3-7 4 14 3-7h5",
        "home" to "M4 11l8-7 8 7v9H4z",
        "moon" to "M20 14.5A8 8 0 0 1 9.5 4a8 8 0 1 0 10.5 10.5z",
        "sun" to "M12 8a4 4 0 1 0 0 8 4 4 0 0 0 0-8zM12 2v2M12 20v2M4 12H2M22 12h-2M5 5l1.5 1.5M17.5 17.5L19 19M5 19l1.5-1.5M17.5 6.5L19 5",
        "pen" to "M4 20l1-4L16 5l3 3L8 19l-4 1zM14 7l3 3",
        "gift" to "M4 10h16v10H4zM3 7h18v3H3zM12 7v13M12 7c-2-4-6-3-4 0M12 7c2-4 6-3 4 0",
        "cross" to "M9 4h6v5h5v6h-5v5H9v-5H4V9h5z",
        "pin" to "M12 21s-6-5.5-6-11a6 6 0 0 1 12 0c0 5.5-6 11-6 11zM12 8a2 2 0 1 0 0 4 2 2 0 0 0 0-4z",
        "star" to "M12 3l2.6 5.6 6 .7-4.4 4.2 1.1 6L12 16.6 6.7 19.5l1.1-6L3.4 9.3l6-.7L12 3z",
        "cake" to "M12 3v4M5 9h14v4H5zM4 13h16v7H4z",
        "flame" to "M12 3c1 3.5 5.5 5.5 5.5 10.5a5.5 5.5 0 0 1-11 0c0-2 1-3.5 2-4.5 0 2 1 3 2 3 0-3.5.5-6 1.5-9z",
        "plus" to "M12 5v14M5 12h14",
        "minus" to "M6 12h12",
        "close" to "M6 6l12 12M18 6L6 18",
        "back" to "M15 5l-7 7 7 7",
        "chevron" to "M9 5l7 7-7 7",
        "trash" to "M5 7h14M10 7V4h4v3M7 7l1 13h8l1-13",
        "bell" to "M6 16V11a6 6 0 0 1 12 0v5l2 2H4zM10 20.5a2 2 0 0 0 4 0",
        "check" to "M5 12.5l4.5 4.5L19 7.5",
        "reset" to "M5 12a7 7 0 1 0 2.2-5.1M5 4v4h4",
        "work" to "M4 8h16v11H4zM9 8V5h6v3M4 13h16",
        "fit" to "M6 8v8M3 10v4M18 8v8M21 10v4M6 12h12",
        "med" to "M12 20c-4 0-8-2-9-6 3 0 6 1 9 4 3-3 6-4 9-4-1 4-5 6-9 6zM12 18c-2-2-3-5-3-8 1 0 2 1 3 3 1-2 2-3 3-3 0 3-1 6-3 8z",
        "read" to "M4 4.5A1.5 1.5 0 0 1 5.5 3H20v14H5.5A1.5 1.5 0 0 0 4 18.5v-14zM4 18.5A1.5 1.5 0 0 0 5.5 20H20",
        "study" to "M2 9l10-5 10 5-10 5zM6 11v5c3 2 9 2 12 0v-5",
        "food" to "M7 3v8M5 3v4a2 2 0 0 0 4 0V3M7 11v10M16 3c-2 0-3 2.5-3 6 0 2 1 3 3 3v9M16 3v9",
        "cart" to "M3 4h2l2.4 11h10.2L20 7H6.2M9 20a1 1 0 1 0 0-.01M17 20a1 1 0 1 0 0-.01",
        "coin" to "M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18zM14.5 9.5c-.5-1-1.5-1.5-2.5-1.5-1.5 0-2.5.8-2.5 2s1 1.7 2.5 2 2.5.8 2.5 2-1 2-2.5 2c-1 0-2-.5-2.5-1.5M12 6.5V8M12 16v1.5",
        "palette" to "M12 3a9 9 0 0 0 0 18c1.1 0 1.7-.9 1.2-1.9-.6-1.1.2-2.1 1.3-2.1H17a4 4 0 0 0 4-4c0-5.5-4-10-9-10zM7.5 11.5v.01M10 7.5v.01M14.5 7.5v.01",
        "people" to "M9 11a3 3 0 1 0 0-6 3 3 0 0 0 0 6zM3 20c0-3.3 2.7-6 6-6s6 2.7 6 6M16 11a2.5 2.5 0 1 0 0-5M17.5 14c2 .5 3.5 2.6 3.5 5",
        "leaf" to "M5 19c0-8 5-14 15-14 0 10-6 15-14 15M5 19l7-7",
        "paw" to "M12 13c-3 0-5 3-5 5s1.5 3 3 3c1 0 1.5-.5 2-.5s1 .5 2 .5c1.5 0 3-1 3-3s-2-5-5-5zM6 8a1.6 1.6 0 1 0 0 3.2A1.6 1.6 0 0 0 6 8zM9.5 4.5a1.6 1.6 0 1 0 0 3.2 1.6 1.6 0 0 0 0-3.2zM14.5 4.5a1.6 1.6 0 1 0 0 3.2 1.6 1.6 0 0 0 0-3.2zM18 8a1.6 1.6 0 1 0 0 3.2A1.6 1.6 0 0 0 18 8z",
        "cook" to "M4 10h16v2a6 6 0 0 1-6 6h-4a6 6 0 0 1-6-6v-2zM2 10h20M9 6c0-1 1-1 1-2M14 6c0-1 1-1 1-2",
        "walk" to "M13 4a1.5 1.5 0 1 0 0 .01M10 21l2-6-2-3 1-5 4 3 3 1M12 15l3 3v3",
        "alert" to "M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18zM12 8v5M12 16.5v.01",
        "prev" to "M19 6v12l-9-6zM6 6v12",
        "next" to "M5 6v12l9-6zM18 6v12",
        "music" to "M9 18V5l11-2v13M9 18a3 3 0 1 1-6 0 3 3 0 0 1 6 0zM20 16a3 3 0 1 1-6 0 3 3 0 0 1 6 0z",
        "play" to "M8 5.5v13l11-6.5z",
        "pause" to "M7 5h3.5v14H7zM13.5 5H17v14h-3.5z"
    )

    fun path(key: String): String = paths[key] ?: key
}

/** Иконка, нарисованная по SVG-контуру. [key] — имя из [Ic] или сам контур. */
@Composable
fun PIcon(
    key: String,
    tint: Color,
    size: Dp = 24.dp,
    stroke: Float = 1.8f,
    filled: Boolean = false,
    modifier: Modifier = Modifier
) {
    val path = remember(key) { PathParser().parsePathString(Ic.path(key)).toPath() }
    Canvas(modifier.size(size)) {
        val s = this.size.minDimension / 24f
        scale(s, s, pivot = Offset.Zero) {
            if (filled) {
                drawPath(path, tint)
            } else {
                drawPath(path, tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }
    }
}
