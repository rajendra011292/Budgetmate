package com.example.budgetmate.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object BM {
    val Teal = Color(0xFF0F766E)
    val TealLight = Color(0xFF14B8A6)
    val Mint = Color(0xFFD1FAE5)
    val Good = Color(0xFF16A34A)
    val Warn = Color(0xFFF59E0B)
    val Bad = Color(0xFFE11D48)
    val Ink = Color(0xFF0B1F1D)

    val heroBrush = Brush.linearGradient(listOf(Color(0xFF0B5F59), Color(0xFF0F766E), Color(0xFF14B8A6)))
    val warnBrush = Brush.linearGradient(listOf(Color(0xFFB45309), Color(0xFFF59E0B)))
    val badBrush = Brush.linearGradient(listOf(Color(0xFF9F1239), Color(0xFFE11D48)))
    val goodBrush = Brush.linearGradient(listOf(Color(0xFF047857), Color(0xFF10B981)))

    /** Envelope accent palette (indexed by colorIndexFor). */
    val palette = listOf(
        Color(0xFF0EA5A4), Color(0xFF6366F1), Color(0xFFF97316), Color(0xFFEC4899),
        Color(0xFF22C55E), Color(0xFF0EA5E9), Color(0xFFA855F7), Color(0xFFEAB308)
    )

    val cardShape = RoundedCornerShape(24.dp)
    val chipShape = RoundedCornerShape(14.dp)
}

private val Light = lightColorScheme(
    primary = BM.Teal, onPrimary = Color.White,
    primaryContainer = Color(0xFFCCF5EF), onPrimaryContainer = Color(0xFF00201D),
    secondary = Color(0xFF4F6B67), secondaryContainer = Color(0xFFD2E8E4),
    background = Color(0xFFF4F7F6), onBackground = BM.Ink,
    surface = Color(0xFFF4F7F6), onSurface = BM.Ink,
    surfaceVariant = Color(0xFFE3ECEA), onSurfaceVariant = Color(0xFF3F4F4D),
    surfaceContainerLowest = Color.White, surfaceContainerLow = Color.White,
    surfaceContainer = Color(0xFFFFFFFF), surfaceContainerHigh = Color(0xFFEAF1EF),
    outlineVariant = Color(0xFFD5E0DE), error = BM.Bad
)

private val Dark = darkColorScheme(
    primary = Color(0xFF4FD8C9), onPrimary = Color(0xFF003732),
    primaryContainer = Color(0xFF0B4A44), onPrimaryContainer = Color(0xFFB9F5ED),
    secondary = Color(0xFFB1CCC7), secondaryContainer = Color(0xFF314B47),
    background = Color(0xFF0E1514), onBackground = Color(0xFFE1ECE9),
    surface = Color(0xFF0E1514), onSurface = Color(0xFFE1ECE9),
    surfaceVariant = Color(0xFF263231), onSurfaceVariant = Color(0xFFB6C7C4),
    surfaceContainerLowest = Color(0xFF0A100F), surfaceContainerLow = Color(0xFF151E1D),
    surfaceContainer = Color(0xFF172120), surfaceContainerHigh = Color(0xFF1E2A29),
    outlineVariant = Color(0xFF34423F), error = Color(0xFFFF6B8A)
)

private val Type = Typography(
    displaySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.ExtraBold, fontSize = 40.sp, letterSpacing = (-1).sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 28.sp, letterSpacing = (-0.5).sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 20.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 12.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 0.5.sp)
)

@Composable
fun BudgetMateTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) Dark else Light, typography = Type, content = content)
}
