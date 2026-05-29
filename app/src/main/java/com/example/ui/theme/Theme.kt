package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private fun getDynamicFalconColorScheme(appTheme: AppThemeValue) = darkColorScheme(
    primary = appTheme.getPrimary(),
    onPrimary = CosmicSpaceBg,
    secondary = appTheme.getSecondary(),
    onSecondary = CosmicSpaceBg,
    tertiary = GoldDim,
    background = CosmicSpaceBg,
    onBackground = SteelGrayText,
    surface = CosmicSlateCard,
    onSurface = SteelGrayText
)

@Composable
fun MyApplicationTheme(
    appTheme: AppThemeValue = AppThemeValue.GOLD,
    content: @Composable () -> Unit,
) {
    val colorScheme = getDynamicFalconColorScheme(appTheme)
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
