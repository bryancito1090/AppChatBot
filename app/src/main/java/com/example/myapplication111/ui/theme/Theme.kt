package com.example.myapplication111.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = DeepBlue,
    secondary = SlateGray,
    background = DarkGray,
    surface = DarkGray,
    onPrimary = PureWhite,
    onSecondary = PureWhite,
    onBackground = PureWhite,
    onSurface = PureWhite
)

private val LightColorScheme = lightColorScheme(
    primary = DeepBlue,
    secondary = SlateGray,
    background = OffWhite,
    surface = LightGray,
    onPrimary = PureWhite,
    onSecondary = PureWhite,
    onBackground = DarkGray,
    onSurface = DarkGray
)

@Composable
fun MyApplication111Theme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
