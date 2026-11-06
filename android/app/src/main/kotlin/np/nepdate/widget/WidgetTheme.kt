package np.nepdate.widget

import android.os.Build
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.background
import androidx.glance.color.DynamicThemeColorProviders
import androidx.glance.material3.ColorProviders
import androidx.glance.unit.ColorProvider
import np.nepdate.ui.theme.DarkColors
import np.nepdate.ui.theme.HolidayContainerDark
import np.nepdate.ui.theme.HolidayContainerLight
import np.nepdate.ui.theme.LightColors
import np.nepdate.ui.theme.OnHolidayContainerDark
import np.nepdate.ui.theme.OnHolidayContainerLight
import np.nepdate.ui.theme.OnToday
import androidx.glance.color.ColorProvider as dayNightColor

/** The app's marigold light and dark schemes as Glance colour providers. */
private val BrandColorProviders = ColorProviders(light = LightColors, dark = DarkColors)

/** True when the surrounding theme follows the wallpaper; the surface containers follow it too. */
private val LocalWallpaperColors = staticCompositionLocalOf { false }

/**
 * The widget theme. Widgets use the app's marigold scheme unless the app setting "Wallpaper colours"
 * is on and the system supports dynamic colour (Android 12+). The holiday and today roles never
 * follow the wallpaper, as in the app.
 */
@Composable
internal fun NepdateGlanceTheme(
    wallpaperColors: Boolean,
    content: @Composable () -> Unit,
) {
    val dynamic = wallpaperColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    CompositionLocalProvider(LocalWallpaperColors provides dynamic) {
        GlanceTheme(colors = if (dynamic) DynamicThemeColorProviders else BrandColorProviders) {
            content()
        }
    }
}

@Composable
internal fun holidayContainerProvider(): ColorProvider =
    dayNightColor(day = HolidayContainerLight, night = HolidayContainerDark)

@Composable
internal fun onHolidayContainerProvider(): ColorProvider =
    dayNightColor(day = OnHolidayContainerLight, night = OnHolidayContainerDark)

/** The ink on the marigold Sunny shape; dark in both themes because the shape stays light. */
@Composable
internal fun onTodayProvider(): ColorProvider = dayNightColor(day = OnToday, night = OnToday)

/** The soft fill of a small control on a coloured widget face. */
@Composable
internal fun chipProvider(): ColorProvider = dayNightColor(day = CHIP_LIGHT, night = CHIP_DARK)

/**
 * Glance's colour set has no surface container roles, so they come from the brand scheme, or from
 * the system neutral palette when the widget follows the wallpaper.
 */
@Composable
internal fun surfaceContainerProvider(): ColorProvider =
    if (LocalWallpaperColors.current && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        systemNeutral(android.R.color.system_neutral1_50, android.R.color.system_neutral1_900)
    } else {
        dayNightColor(day = LightColors.surfaceContainer, night = DarkColors.surfaceContainer)
    }

@Composable
internal fun surfaceContainerHighestProvider(): ColorProvider =
    if (LocalWallpaperColors.current && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        systemNeutral(android.R.color.system_neutral1_100, android.R.color.system_neutral1_800)
    } else {
        dayNightColor(day = LightColors.surfaceContainerHighest, night = DarkColors.surfaceContainerHighest)
    }

@Composable
internal fun outlineVariantProvider(): ColorProvider =
    if (LocalWallpaperColors.current) {
        GlanceTheme.colors.outline
    } else {
        dayNightColor(day = LightColors.outlineVariant, night = DarkColors.outlineVariant)
    }

@Composable
private fun systemNeutral(
    day: Int,
    night: Int,
): ColorProvider {
    val context = LocalContext.current
    return dayNightColor(day = Color(context.getColor(day)), night = Color(context.getColor(night)))
}

/**
 * A rounded shape behind a view. Glance can only round all four corners alike, and only on Android
 * 12+, so the shapes are white drawables tinted with [color]; this also covers connected button
 * groups whose inner corners are smaller than their outer ones.
 */
internal fun GlanceModifier.shapeBackground(
    @DrawableRes shape: Int,
    color: ColorProvider,
): GlanceModifier = background(imageProvider = ImageProvider(shape), colorFilter = ColorFilter.tint(color))

private val CHIP_LIGHT = Color(0x14271900)
private val CHIP_DARK = Color(0x1FFFFFFF)
