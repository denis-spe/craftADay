package com.den.craftaday.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        text = Color.Unspecified,
        background = Color.Unspecified,
        primary = Color.Unspecified,
        secondary = Color.Unspecified
    )
}

@Composable
fun CraftADayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val extendedColors = if (darkTheme) {
        ExtendedColors(
            text = Color.White,
            background = Color(0xFF252525),
            primary = Color(0xFF9C27B0),
            secondary = Color(0xFF1F79D7)
        )
    } else {
        ExtendedColors(
            text = Color.Black,
            background = Color(0xFFF5F5F5),
            primary = AppStartColor,
            secondary = Color(0xFFC54E29)
        )
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme)
                dynamicDarkColorScheme(context).copy(
                    primary = extendedColors.primary,
                    secondary = extendedColors.secondary,
                    background = extendedColors.background,
                    surface = extendedColors.background,
                    onBackground = extendedColors.text,
                    onSurface = extendedColors.text
                )
            else dynamicLightColorScheme(context).copy(
                primary = extendedColors.primary,
                secondary = extendedColors.secondary,
                background = extendedColors.background,
                surface = extendedColors.background,
                onBackground = extendedColors.text,
                onSurface = extendedColors.text
            )
        }

        darkTheme -> DarkColorScheme.copy(
            primary = extendedColors.primary,
            secondary = extendedColors.secondary,
            background = extendedColors.background,
            surface = extendedColors.background,
            onBackground = extendedColors.text,
            onSurface = extendedColors.text
        )

        else -> LightColorScheme.copy(
            primary = extendedColors.primary,
            secondary = extendedColors.secondary,
            background = extendedColors.background,
            surface = extendedColors.background,
            onBackground = extendedColors.text,
            onSurface = extendedColors.text
        )
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

object ExtendedTheme {
    val colors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}