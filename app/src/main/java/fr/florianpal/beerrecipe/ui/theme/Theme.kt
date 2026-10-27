package fr.florianpal.beerrecipe.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Palette inspirée du moût : ambre, cuivre et grain torréfié. */
private val Amber = Color(0xFFB9742B)
private val AmberDark = Color(0xFFE3A05A)
private val Copper = Color(0xFF8C4A1E)
private val Hop = Color(0xFF5B7F3A)
private val HopDark = Color(0xFF9CC06F)
private val Malt = Color(0xFF3E2A1A)
private val Foam = Color(0xFFFFF8EC)

private val LightColors = lightColorScheme(
    primary = Amber,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDDB5),
    onPrimaryContainer = Color(0xFF2C1600),
    secondary = Hop,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD9EFC0),
    onSecondaryContainer = Color(0xFF162004),
    tertiary = Copper,
    onTertiary = Color.White,
    background = Foam,
    onBackground = Malt,
    surface = Foam,
    onSurface = Malt,
    surfaceVariant = Color(0xFFF2E3D0),
    onSurfaceVariant = Color(0xFF52443A),
    outline = Color(0xFF857468),
    error = Color(0xFFB3261E),
)

private val DarkColors = darkColorScheme(
    primary = AmberDark,
    onPrimary = Color(0xFF452B00),
    primaryContainer = Color(0xFF633F00),
    onPrimaryContainer = Color(0xFFFFDDB5),
    secondary = HopDark,
    onSecondary = Color(0xFF203600),
    secondaryContainer = Color(0xFF3D5321),
    onSecondaryContainer = Color(0xFFD9EFC0),
    tertiary = Color(0xFFFFB68C),
    onTertiary = Color(0xFF521F00),
    background = Color(0xFF1A120B),
    onBackground = Color(0xFFF0E0D0),
    surface = Color(0xFF1A120B),
    onSurface = Color(0xFFF0E0D0),
    surfaceVariant = Color(0xFF52443A),
    onSurfaceVariant = Color(0xFFD7C3B4),
    outline = Color(0xFF9F8D80),
    error = Color(0xFFF2B8B5),
)

private val AppTypography = Typography(
    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
)

@Composable
fun BeerRecipeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = MaterialTheme.typography.copy(
            titleLarge = AppTypography.titleLarge,
            titleMedium = AppTypography.titleMedium,
            labelLarge = AppTypography.labelLarge,
        ),
        content = content,
    )
}
