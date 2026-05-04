package com.underthykilt.poketypes.ui.theme

import androidx.compose.ui.graphics.Color

val CorrectGreen = Color(0xFF1A6B1A)
val WrongRed = Color(0xFF8B1A1A)
val QuizDualColor = Color(0xFF7038F8)

fun effectivenessColor(mult: Float): Color = when (mult) {
    0f -> Color(0xFF222222)
    0.5f -> WrongRed
    2f -> CorrectGreen
    else -> Color(0xFF2A2A3A)
}

fun scoreColor(score: Int, total: Int = 10): Color {
    if (total == 0) return WrongRed
    val pct = score * 100 / total
    return when {
        pct >= 90 -> CorrectGreen
        pct >= 70 -> Color(0xFFC8960C)
        pct >= 50 -> Color(0xFFE65100)
        else      -> WrongRed
    }
}
