package userinterface.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AppColorPalette = lightColors(
    primary = Color(58,174,244, 255),
    onPrimary = Color.White,
    primaryVariant = Color(236, 255, 255, 255),
    secondary = Color(215,95,160,255),
    onSecondary = Color.White,
    secondaryVariant = Color(255,236,236, 255),
    background = Color(200, 255, 255, 95),
)

val AppTypography = Typography()

@Composable
fun ProvideAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = AppColorPalette,
        typography = AppTypography,
        content = content
    )
}