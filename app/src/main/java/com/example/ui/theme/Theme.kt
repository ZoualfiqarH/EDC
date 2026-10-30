package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = DentalTealPrimaryDark,
    onPrimary = Color(0xFF00363F),
    primaryContainer = DentalTealContainerDark,
    onPrimaryContainer = Color(0xFFA6EEFF),
    secondary = DentalMintSecondaryDark,
    onSecondary = Color(0xFF00382B),
    secondaryContainer = DentalMintContainerDark,
    onSecondaryContainer = Color(0xFFAAF2D7),
    tertiary = DentalGoldTertiaryDark,
    onTertiary = Color(0xFF452B00),
    tertiaryContainer = DentalGoldContainerDark,
    onTertiaryContainer = Color(0xFFFFDDB3),
    background = ClinicalBackgroundDark,
    onBackground = ClinicalTextPrimaryDark,
    surface = ClinicalSurfaceDark,
    onSurface = ClinicalTextPrimaryDark,
    surfaceVariant = ClinicalSurfaceVariantDark,
    onSurfaceVariant = ClinicalTextSecondaryDark,
    error = MedicalAlert,
    errorContainer = MedicalAlertContainer,
    onErrorContainer = MedicalAlertOnContainer
  )

private val LightColorScheme =
  lightColorScheme(
    primary = DentalTealPrimary,
    onPrimary = DentalTealOnPrimary,
    primaryContainer = DentalTealContainer,
    onPrimaryContainer = DentalTealOnContainer,
    secondary = DentalMintSecondary,
    onSecondary = DentalMintOnSecondary,
    secondaryContainer = DentalMintContainer,
    onSecondaryContainer = DentalMintOnContainer,
    tertiary = DentalGoldTertiary,
    onTertiary = Color.White,
    tertiaryContainer = DentalGoldContainer,
    onTertiaryContainer = DentalGoldOnContainer,
    background = ClinicalBackgroundLight,
    onBackground = ClinicalTextPrimaryLight,
    surface = ClinicalSurfaceLight,
    onSurface = ClinicalTextPrimaryLight,
    surfaceVariant = ClinicalSurfaceVariantLight,
    onSurfaceVariant = ClinicalTextSecondaryLight,
    error = MedicalAlert,
    errorContainer = MedicalAlertContainer,
    onErrorContainer = MedicalAlertOnContainer
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Use our tailored dental branding colors by default
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
