package com.example.planer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color as AColor
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import com.example.planer.data.Ads
import com.example.planer.data.AppState
import com.example.planer.data.FocusTimer
import com.example.planer.data.Music
import com.example.planer.data.Reminders
import com.example.planer.data.ThemeMode
import com.example.planer.ui.theme.PlanerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = AppState.get(this)
        Reminders.createChannel(this)
        Reminders.scheduleNext(this)
        app.restoreTimer()
        openFocusFrom(intent)
        Ads.init(this)
        setContent {
            val systemDark = isSystemInDarkTheme()
            val dark = when (app.data.theme) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.AUTO -> systemDark
            }
            LaunchedEffect(dark) {
                val style = if (dark) {
                    SystemBarStyle.dark(AColor.TRANSPARENT)
                } else {
                    SystemBarStyle.light(AColor.TRANSPARENT, AColor.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            }

            // Разрешение на уведомления (Android 13+) спрашиваем после приветствия.
            val askNotifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) Reminders.scheduleNext(this@MainActivity)
            }
            LaunchedEffect(app.data.onboarded) {
                if (app.data.onboarded && Build.VERSION.SDK_INT >= 33 &&
                    ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                ) {
                    askNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            val nav = remember { Nav(if (app.data.onboarded) Screen.Main(Tab.TODAY) else Screen.Welcome) }
            PlanerTheme(dark) {
                PlanerApp(app, nav)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Music.connect(this)
    }

    override fun onStop() {
        super.onStop()
        Music.disconnect()
    }

    override fun onResume() {
        super.onResume()
        val app = AppState.get(this)
        app.markVisit()
        app.restoreTimer() // время таймера пересчитывается после возврата в приложение
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        openFocusFrom(intent)
    }

    /** Нажали на уведомление таймера — открываем вкладку «Фокус». */
    private fun openFocusFrom(intent: Intent?) {
        if (intent?.getBooleanExtra(FocusTimer.EXTRA_OPEN_FOCUS, false) == true) {
            intent.removeExtra(FocusTimer.EXTRA_OPEN_FOCUS)
            AppState.get(this).openFocusRequest++
        }
    }

    override fun onPause() {
        super.onPause()
        val app = AppState.get(this)
        app.tick()
        app.flush(false)
    }
}
