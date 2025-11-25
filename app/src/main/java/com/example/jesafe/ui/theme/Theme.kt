package com.example.jesafe.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = DeepBlue,
    onPrimary = OffWhite,
    secondary = SoftBlue,
    onSecondary = NavyBlue,
    tertiary = CobaltBlue,
    onTertiary = NavyBlue,

    background = NavyBlue,
    onBackground = OffWhite,

    surface = CharcoalGrey,
    onSurface = OffWhite,

    outline = LightGrey
)

private val LightColorScheme = lightColorScheme(
    primary = SoftBlue,         // 가장 대표 컬러
    onPrimary = OffWhite,       // primary 위에 올라갈 텍스트/아이콘

    secondary = CobaltBlue,     // 보조 컬러
    onSecondary = OffWhite,

    tertiary = DeepBlue,        // 액센트 컬러
    onTertiary = OffWhite,

    background = White,
    onBackground = Black,

    surface = White,
    onSurface = Black,

    outline = LightGrey
)

@Composable
fun JeSafeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = White.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}