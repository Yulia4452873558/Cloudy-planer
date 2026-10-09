package com.example.planer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.planer.Nav
import com.example.planer.Tab
import com.example.planer.data.AppData
import com.example.planer.data.AppState
import com.example.planer.ui.components.Field
import com.example.planer.ui.components.IconTile
import com.example.planer.ui.components.Mascot
import com.example.planer.ui.components.PrimaryButton
import com.example.planer.ui.components.T
import com.example.planer.ui.components.card
import java.time.LocalDate

/** Первый экран. Всё помещается на один экран, без прокрутки. */
@Composable
fun WelcomeScreen(app: AppState, nav: Nav) {
    var name by rememberSaveable { mutableStateOf("") }
    val ink = Color(0xFF2A2440)
    Box(
        Modifier.fillMaxSize()
            .background(Brush.linearGradient(listOf(Color(0xFFCFE6F9), Color(0xFFDCD2F7), Color(0xFFF6D6EC))))
    ) {
        Column(
            Modifier.fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))
            Mascot(k = 1.6f)
            Spacer(Modifier.height(16.dp))
            T("Привет! Я Облачко", 30, FontWeight.ExtraBold, ink, align = TextAlign.Center, letterSpacing = -0.5f)
            Spacer(Modifier.height(8.dp))
            T(
                "Помогу спланировать день, не забыть о себе и порадоваться каждой маленькой победе.",
                15, FontWeight.SemiBold, Color(0xFF4F4866), align = TextAlign.Center, lineHeight = 21
            )
            Spacer(Modifier.weight(1f))
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Feature("today", Color(0xFFFDE7F3), "Задачи, привычки и расписание")
                Feature("heart", Color(0xFFE6F2FD), "Цикл, вода, сон и настроение")
                Feature("timer", Color(0xFFEFE8FD), "Фокус-таймер и награды")
            }
            Spacer(Modifier.weight(1f))
            Field(name, { name = it.take(24) }, "Как тебя зовут?", Modifier.fillMaxWidth(), bg = Color.White.copy(alpha = 0.75f))
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.width(22.dp).height(6.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFF6F52C9)))
                Box(Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(Color(0x4D6F52C9)))
                Box(Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(Color(0x4D6F52C9)))
            }
            Spacer(Modifier.height(14.dp))
            PrimaryButton("Начать", {
                val n = name.trim()
                val today = LocalDate.now()
                app.update { AppData(onboarded = true, name = n, theme = it.theme, visits = setOf(today)) }
                nav.tab(Tab.TODAY)
            })
        }
    }
}

@Composable
private fun Feature(icon: String, bg: Color, text: String) {
    Row(
        Modifier.fillMaxWidth().card(Color.White.copy(alpha = 0.62f), 22.dp).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconTile(icon, bg, size = 40.dp, radius = 14.dp, tint = Color(0xFF2A2440), iconSize = 20.dp)
        T(text, 15, FontWeight.Bold, Color(0xFF2A2440))
    }
}
