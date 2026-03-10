package com.indoora.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun Modifier.indooraBackground(): Modifier {
    val isDark = isSystemInDarkTheme()

    return this.background(
        brush = if (isDark) {
            Brush.radialGradient(
                colors = listOf(
                    Color(0xFF5E6EFB),
                    Color(0xFFBCA4FF)
                )
            )
        } else {
            Brush.radialGradient(
                colors = listOf(
                    Color(0xFF9176DA),
                    Color(0xFF31118A)
                )
            )
        }
    )
}