package co.ke.xently.expense.tracker.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Sugarcane Green & Earth Palette
private val GreenPrimaryLight = Color(0xFF2E6B34)
private val GreenOnPrimaryLight = Color(0xFFFFFFFF)
private val GreenPrimaryContainerLight = Color(0xFFB1F4AE)
private val GreenOnPrimaryContainerLight = Color(0xFF002206)

private val GreenSecondaryLight = Color(0xFF52634F)
private val GreenOnSecondaryLight = Color(0xFFFFFFFF)
private val GreenSecondaryContainerLight = Color(0xFFD5E8CF)
private val GreenOnSecondaryContainerLight = Color(0xFF101F10)

private val GreenTertiaryLight = Color(0xFF386568)
private val GreenOnTertiaryLight = Color(0xFFFFFFFF)
private val GreenTertiaryContainerLight = Color(0xFFBCEBEF)
private val GreenOnTertiaryContainerLight = Color(0xFF002022)

private val ErrorLight = Color(0xFFBA1A1A)
private val OnErrorLight = Color(0xFFFFFFFF)
private val ErrorContainerLight = Color(0xFFFFDAD6)
private val OnErrorContainerLight = Color(0xFF410002)

private val BackgroundLight = Color(0xFFF7FBF1)
private val OnBackgroundLight = Color(0xFF181D17)
private val SurfaceLight = Color(0xFFF7FBF1)
private val OnSurfaceLight = Color(0xFF181D17)
private val SurfaceVariantLight = Color(0xFFDEE5D8)
private val OnSurfaceVariantLight = Color(0xFF424940)

// Dark Palette
private val GreenPrimaryDark = Color(0xFF96D794)
private val GreenOnPrimaryDark = Color(0xFF00390F)
private val GreenPrimaryContainerDark = Color(0xFF145220)
private val GreenOnPrimaryContainerDark = Color(0xFFB1F4AE)

private val GreenSecondaryDark = Color(0xFFB9CCB4)
private val GreenOnSecondaryDark = Color(0xFF253423)
private val GreenSecondaryContainerDark = Color(0xFF3B4B38)
private val GreenOnSecondaryContainerDark = Color(0xFFD5E8CF)

private val GreenTertiaryDark = Color(0xFFA0CFD2)
private val GreenOnTertiaryDark = Color(0xFF003639)
private val GreenTertiaryContainerDark = Color(0xFF1E4D50)
private val GreenOnTertiaryContainerDark = Color(0xFFBCEBEF)

private val ErrorDark = Color(0xFFFFB4AB)
private val OnErrorDark = Color(0xFF690005)
private val ErrorContainerDark = Color(0xFF93000A)
private val OnErrorContainerDark = Color(0xFFFFDAD6)

private val BackgroundDark = Color(0xFF101510)
private val OnBackgroundDark = Color(0xFFE0E4DB)
private val SurfaceDark = Color(0xFF101510)
private val OnSurfaceDark = Color(0xFFE0E4DB)
private val SurfaceVariantDark = Color(0xFF424940)
private val OnSurfaceVariantDark = Color(0xFFC2C9BD)

// Semantic colors
val ComplianceGreen = Color(0xFF2E7D32)
val ComplianceAmber = Color(0xFFED6C02)
val ComplianceRed = Color(0xFFD32F2F)

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimaryLight,
    onPrimary = GreenOnPrimaryLight,
    primaryContainer = GreenPrimaryContainerLight,
    onPrimaryContainer = GreenOnPrimaryContainerLight,
    secondary = GreenSecondaryLight,
    onSecondary = GreenOnSecondaryLight,
    secondaryContainer = GreenSecondaryContainerLight,
    onSecondaryContainer = GreenOnSecondaryContainerLight,
    tertiary = GreenTertiaryLight,
    onTertiary = GreenOnTertiaryLight,
    tertiaryContainer = GreenTertiaryContainerLight,
    onTertiaryContainer = GreenOnTertiaryContainerLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight
)

private val DarkColorScheme = darkColorScheme(
    primary = GreenPrimaryDark,
    onPrimary = GreenOnPrimaryDark,
    primaryContainer = GreenPrimaryContainerDark,
    onPrimaryContainer = GreenOnPrimaryContainerDark,
    secondary = GreenSecondaryDark,
    onSecondary = GreenOnSecondaryDark,
    secondaryContainer = GreenSecondaryContainerDark,
    onSecondaryContainer = GreenOnSecondaryContainerDark,
    tertiary = GreenTertiaryDark,
    onTertiary = GreenOnTertiaryDark,
    tertiaryContainer = GreenTertiaryContainerDark,
    onTertiaryContainer = GreenOnTertiaryContainerDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark
)

@Composable
fun SugarcaneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
