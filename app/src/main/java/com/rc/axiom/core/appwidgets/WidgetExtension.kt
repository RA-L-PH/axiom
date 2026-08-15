package com.rc.axiom.core.appwidgets

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.TypedValue
import android.view.KeyEvent
import androidx.annotation.ColorInt
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.glance.GlanceTheme
import androidx.glance.action.Action
import androidx.glance.appwidget.action.actionStartService
import androidx.glance.color.ColorProvider
import androidx.glance.color.ColorProviders
import androidx.glance.color.colorProviders
import androidx.glance.text.FontFamily
import androidx.glance.text.TextStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.TextAlign
import androidx.compose.ui.unit.TextUnit
import com.rc.axiom.core.appwidgets.state.WidgetTheme
import com.rc.axiom.playback.PlaybackService
import com.rc.axiom.ui.theme.PaletteStyle
import com.rc.axiom.ui.theme.dynamicColorSchemes

fun Dp.toPx(context: Context): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.value,
        context.resources.displayMetrics
    ).toInt()
}

fun WidgetTheme(
    @ColorInt
    sourceColor: Int,
    style: PaletteStyle = PaletteStyle.Fidelity
): WidgetTheme {
    val colorSchemes = dynamicColorSchemes(
        keyColor = Color(sourceColor),
        style = style,
        contrastLevel = 0.75
    )
    return WidgetTheme(
        lightSurfaceColor = colorSchemes.lightColorScheme.surface.toArgb(),
        lightOnSurfaceColor = colorSchemes.lightColorScheme.onSurface.toArgb(),
        lightOnSurfaceVariantColor = colorSchemes.lightColorScheme.onSurfaceVariant.toArgb(),
        lightPrimaryColor = colorSchemes.lightColorScheme.primary.toArgb(),
        lightOnPrimaryColor = colorSchemes.lightColorScheme.onPrimary.toArgb(),
        lightPrimaryContainerColor = colorSchemes.lightColorScheme.primaryContainer.toArgb(),
        lightOnPrimaryContainerColor = colorSchemes.lightColorScheme.onPrimaryContainer.toArgb(),
        lightTertiaryContainerColor = colorSchemes.lightColorScheme.tertiaryContainer.toArgb(),
        lightOnTertiaryContainerColor = colorSchemes.lightColorScheme.onTertiaryContainer.toArgb(),
        darkSurfaceColor = colorSchemes.darkColorScheme.surface.toArgb(),
        darkOnSurfaceColor = colorSchemes.darkColorScheme.onSurface.toArgb(),
        darkOnSurfaceVariantColor = colorSchemes.darkColorScheme.onSurfaceVariant.toArgb(),
        darkPrimaryColor = colorSchemes.darkColorScheme.primary.toArgb(),
        darkOnPrimaryColor = colorSchemes.darkColorScheme.onPrimary.toArgb(),
        darkPrimaryContainerColor = colorSchemes.darkColorScheme.primaryContainer.toArgb(),
        darkOnPrimaryContainerColor = colorSchemes.darkColorScheme.onPrimaryContainer.toArgb(),
        darkTertiaryContainerColor = colorSchemes.darkColorScheme.tertiaryContainer.toArgb(),
        darkOnTertiaryContainerColor = colorSchemes.lightColorScheme.onTertiaryContainer.toArgb()
    )
}

@Composable
fun WidgetTheme?.getColors(): ColorProviders {
    val pureBlack = Color(0xFF000000)
    val pureWhite = Color(0xFFFFFFFF)
    val darkGray = Color(0xFF121212)
    val lightGray = Color(0xFF242424)
    val nothingRed = Color(0xFFFF0800)
    val themeColors = GlanceTheme.colors

    val primaryProvider = ColorProvider(day = pureWhite, night = pureWhite)
    val onPrimaryProvider = ColorProvider(day = pureBlack, night = pureBlack)
    val primaryContainerProvider = ColorProvider(day = darkGray, night = darkGray)
    val onPrimaryContainerProvider = ColorProvider(day = pureWhite, night = pureWhite)
    val secondaryProvider = ColorProvider(day = nothingRed, night = nothingRed)
    val onSecondaryProvider = ColorProvider(day = pureWhite, night = pureWhite)
    val secondaryContainerProvider = ColorProvider(day = lightGray, night = lightGray)
    val onSecondaryContainerProvider = ColorProvider(day = pureWhite, night = pureWhite)
    val backgroundProvider = ColorProvider(day = pureBlack, night = pureBlack)
    val onBackgroundProvider = ColorProvider(day = pureWhite, night = pureWhite)
    val surfaceProvider = ColorProvider(day = darkGray, night = darkGray)
    val onSurfaceProvider = ColorProvider(day = pureWhite, night = pureWhite)
    val surfaceVariantProvider = ColorProvider(day = pureBlack, night = pureBlack)
    val onSurfaceVariantProvider = ColorProvider(day = Color(0xFF888888), night = Color(0xFF888888))
    val outlineProvider = ColorProvider(day = Color(0xFF333333), night = Color(0xFF333333))

    return colorProviders(
        primary = primaryProvider,
        onPrimary = onPrimaryProvider,
        primaryContainer = primaryContainerProvider,
        onPrimaryContainer = onPrimaryContainerProvider,
        secondary = secondaryProvider,
        onSecondary = onSecondaryProvider,
        secondaryContainer = secondaryContainerProvider,
        onSecondaryContainer = onSecondaryContainerProvider,
        tertiary = themeColors.tertiary,
        onTertiary = themeColors.onTertiary,
        tertiaryContainer = secondaryContainerProvider,
        onTertiaryContainer = onSecondaryContainerProvider,
        error = themeColors.error,
        errorContainer = themeColors.errorContainer,
        onError = themeColors.onError,
        onErrorContainer = themeColors.onErrorContainer,
        background = backgroundProvider,
        onBackground = onBackgroundProvider,
        surface = surfaceProvider,
        onSurface = onSurfaceProvider,
        surfaceVariant = surfaceVariantProvider,
        onSurfaceVariant = onSurfaceVariantProvider,
        outline = outlineProvider,
        inverseOnSurface = themeColors.inverseOnSurface,
        inverseSurface = themeColors.inverseSurface,
        inversePrimary = themeColors.inversePrimary,
        widgetBackground = backgroundProvider
    )
}

fun playbackAction(context: Context, mediaKeyCode: Int): Action {
    val intent = Intent(Intent.ACTION_MEDIA_BUTTON)
    intent.setComponent(ComponentName(context, PlaybackService::class.java))
    intent.putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_DOWN, mediaKeyCode))
    return actionStartService(intent, true)
}

fun toggleShuffleAction(context: Context): Action {
    val intent = Intent(PlaybackService.ACTION_TOGGLE_SHUFFLE)
    intent.setComponent(ComponentName(context, PlaybackService::class.java))
    return actionStartService(intent)
}

fun cycleRepeatAction(context: Context): Action {
    val intent = Intent(PlaybackService.ACTION_CYCLE_REPEAT)
    intent.setComponent(ComponentName(context, PlaybackService::class.java))
    return actionStartService(intent)
}

fun toggleFavoriteAction(context: Context): Action {
    val intent = Intent(PlaybackService.ACTION_TOGGLE_FAVORITE)
    intent.setComponent(ComponentName(context, PlaybackService::class.java))
    return actionStartService(intent)
}

fun NothingTextStyle(
    color: androidx.glance.unit.ColorProvider? = null,
    fontSize: TextUnit? = null,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null
): TextStyle {
    return if (color != null) {
        TextStyle(
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight,
            fontFamily = FontFamily.Monospace,
            textAlign = textAlign
        )
    } else {
        TextStyle(
            fontSize = fontSize,
            fontWeight = fontWeight,
            fontFamily = FontFamily.Monospace,
            textAlign = textAlign
        )
    }
}