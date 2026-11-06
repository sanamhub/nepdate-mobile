package np.nepdate.widget

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.semantics.testTag
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import np.nepdate.R

/**
 * The Next festival widget: days left as a big number, then the name and the BS date with its
 * weekday. Public holidays sit on the holiday container, other festivals on the secondary
 * container. Two connected arrows step through the next festivals; [FestivalStepAction] stores the
 * position per widget and the midnight refresh clears it.
 */
@Composable
internal fun NextFestivalWidgetContent(
    model: NextFestivalModel,
    opacity: WidgetOpacity,
) {
    val context = LocalContext.current
    val background = if (model.isHoliday) holidayContainerProvider() else GlanceTheme.colors.secondaryContainer
    val ink = if (model.isHoliday) onHolidayContainerProvider() else GlanceTheme.colors.onSecondaryContainer
    val label = context.getString(R.string.widget2_next_festival_name)
    val compact = LocalSize.current.height < COMPACT_HEIGHT
    Box(
        modifier =
            widgetRoot(opacity, background)
                .clickable(actionStartActivity(widgetIntent(context, model.entry?.date)))
                .semantics {
                    contentDescription = label
                    testTag = "next_festival_widget"
                },
    ) {
        when {
            model.entry == null -> EmptyText(model.emptyText, ink)
            compact -> NextFestivalCompact(model, ink)
            else -> NextFestivalFull(model, ink)
        }
    }
}

@Composable
private fun NextFestivalFull(
    model: NextFestivalModel,
    ink: ColorProvider,
) {
    val entry = model.entry ?: return
    val tall = LocalSize.current.height >= TALL_HEIGHT
    Column(modifier = GlanceModifier.fillMaxSize().padding(start = 16.dp, end = 10.dp, top = 4.dp, bottom = 14.dp)) {
        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = model.kind,
                modifier = GlanceModifier.defaultWeight(),
                style = TextStyle(color = ink, fontSize = 12.sp, fontWeight = FontWeight.Bold),
                maxLines = 1,
            )
            FestivalArrows(model, ink)
        }
        Spacer(GlanceModifier.defaultWeight())
        DaysLeft(model, ink, numberSize = if (tall) 60.sp else 40.sp)
        Text(
            text = entry.name,
            style = TextStyle(color = ink, fontSize = if (tall) 18.sp else 15.sp, fontWeight = FontWeight.Bold),
            maxLines = if (tall) 2 else 1,
        )
        Text(
            text = model.dateLine,
            style = TextStyle(color = ink, fontSize = 12.sp),
            maxLines = 1,
        )
    }
}

/** The 2x1 face: number and name on the left, the arrows on the right. */
@Composable
private fun NextFestivalCompact(
    model: NextFestivalModel,
    ink: ColorProvider,
) {
    val entry = model.entry ?: return
    Row(
        modifier = GlanceModifier.fillMaxSize().padding(start = 12.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DaysLeft(model, ink, numberSize = 26.sp)
        Text(
            text = entry.name,
            modifier = GlanceModifier.defaultWeight().padding(start = 8.dp),
            style = TextStyle(color = ink, fontSize = 13.sp, fontWeight = FontWeight.Bold),
            maxLines = 2,
        )
        FestivalArrows(model, ink)
    }
}

@Composable
private fun DaysLeft(
    model: NextFestivalModel,
    ink: ColorProvider,
    numberSize: TextUnit,
) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = model.days,
            modifier = GlanceModifier.semantics { testTag = "next_festival_days" },
            style = TextStyle(color = ink, fontSize = numberSize, fontWeight = FontWeight.Bold),
            maxLines = 1,
        )
        if (model.unit.isNotEmpty()) {
            Text(
                text = model.unit,
                modifier = GlanceModifier.padding(start = 4.dp, bottom = 4.dp),
                style = TextStyle(color = ink, fontSize = 14.sp, fontWeight = FontWeight.Bold),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun FestivalArrows(
    model: NextFestivalModel,
    ink: ColorProvider,
) {
    val context = LocalContext.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        ConnectedIconButton(
            shape = R.drawable.widget_shape_start,
            icon = R.drawable.ic_sym_chevron_left,
            width = 36.dp,
            fill = chipProvider(),
            ink = ink,
            description = context.getString(R.string.widget2_previous_festival),
            tag = "next_festival_previous",
            index = model.previousIndex,
        )
        Spacer(GlanceModifier.width(2.dp))
        ConnectedIconButton(
            shape = R.drawable.widget_shape_end,
            icon = R.drawable.ic_sym_chevron_right,
            width = 36.dp,
            fill = chipProvider(),
            ink = ink,
            description = context.getString(R.string.widget2_following_festival),
            tag = "next_festival_next",
            index = model.nextIndex,
        )
    }
}

/** One arrow of a connected pair: a 32 dp tall chip inside a 48 dp tall touch target. */
@Composable
private fun ConnectedIconButton(
    @DrawableRes shape: Int,
    @DrawableRes icon: Int,
    width: Dp,
    fill: ColorProvider,
    ink: ColorProvider,
    description: String,
    tag: String,
    index: Int?,
) {
    val action = index?.let { actionRunCallback<FestivalStepAction>(actionParametersOf(FESTIVAL_INDEX_PARAM to it)) }
    Box(
        modifier =
            GlanceModifier
                .width(width)
                .height(TOUCH_TARGET)
                .then(if (action != null) GlanceModifier.clickable(action) else GlanceModifier)
                .semantics {
                    contentDescription = description
                    testTag = tag
                },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = GlanceModifier.width(width).height(32.dp).shapeBackground(shape, fill),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                provider = ImageProvider(icon),
                contentDescription = null,
                modifier = GlanceModifier.size(20.dp),
                colorFilter = ColorFilter.tint(ink),
            )
        }
    }
}

/**
 * The Festivals list widget: the next three or four entries as a connected list, with a "holidays
 * only" filter chip that [HolidayFilterAction] toggles per widget. A row opens its day in the app.
 */
@Composable
internal fun FestivalsWidgetContent(
    model: FestivalsListModel,
    opacity: WidgetOpacity,
) {
    val context = LocalContext.current
    val height = LocalSize.current.height
    val rowHeight =
        when {
            height >= ROOMY_LIST_HEIGHT -> 46.dp
            height >= MEDIUM_LIST_HEIGHT -> 40.dp
            else -> 36.dp
        }
    val available = height - LIST_CHROME
    val fitting = (available.value / (rowHeight.value + ROW_GAP.value)).toInt().coerceIn(1, FESTIVALS_MAX_ROWS)
    val rows = model.rows.take(fitting)
    Column(
        modifier =
            widgetRoot(opacity, GlanceTheme.colors.surface)
                .clickable(actionStartActivity(widgetIntent(context)))
                .semantics { testTag = "festivals_widget" }
                .padding(start = 12.dp, end = 12.dp, top = 2.dp, bottom = 12.dp),
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(start = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = context.getString(R.string.widget2_festivals_title),
                modifier = GlanceModifier.defaultWeight(),
                style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                maxLines = 1,
            )
            HolidaysOnlyChip(model.holidaysOnly)
        }
        if (rows.isEmpty()) {
            Text(
                text = model.emptyText,
                modifier = GlanceModifier.padding(start = 6.dp, top = 4.dp),
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 13.sp),
                maxLines = 2,
            )
        }
        rows.forEachIndexed { index, row ->
            if (index > 0) Spacer(GlanceModifier.height(ROW_GAP))
            FestivalListRow(row, index, rows.size, rowHeight)
        }
    }
}

@Composable
private fun HolidaysOnlyChip(holidaysOnly: Boolean) {
    val context = LocalContext.current
    val description =
        context.getString(if (holidaysOnly) R.string.widget2_holidays_only_on else R.string.widget2_holidays_only_off)
    val ink = if (holidaysOnly) GlanceTheme.colors.onSecondaryContainer else GlanceTheme.colors.onSurfaceVariant
    Box(
        modifier =
            GlanceModifier
                .height(TOUCH_TARGET)
                .clickable(actionRunCallback<HolidayFilterAction>())
                .semantics {
                    contentDescription = description
                    testTag = "festivals_filter"
                },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier =
                GlanceModifier
                    .height(32.dp)
                    .then(
                        if (holidaysOnly) {
                            GlanceModifier.shapeBackground(
                                R.drawable.widget_shape_chip,
                                GlanceTheme.colors.secondaryContainer,
                            )
                        } else {
                            GlanceModifier.shapeBackground(
                                R.drawable.widget_shape_chip_outline,
                                outlineVariantProvider(),
                            )
                        },
                    ).padding(start = 8.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                provider = ImageProvider(if (holidaysOnly) R.drawable.ic_sym_check else R.drawable.ic_sym_filter_alt),
                contentDescription = null,
                modifier = GlanceModifier.size(18.dp),
                colorFilter = ColorFilter.tint(ink),
            )
            Text(
                text = context.getString(R.string.widget2_holidays_only),
                modifier = GlanceModifier.padding(start = 4.dp),
                style = TextStyle(color = ink, fontSize = 13.sp, fontWeight = FontWeight.Bold),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun FestivalListRow(
    row: FestivalRow,
    index: Int,
    count: Int,
    height: Dp,
) {
    val context = LocalContext.current
    val shape =
        when {
            count == 1 -> R.drawable.widget_shape_18
            index == 0 -> R.drawable.widget_shape_top
            index == count - 1 -> R.drawable.widget_shape_bottom
            else -> R.drawable.widget_shape_4
        }
    Row(
        modifier =
            GlanceModifier
                .fillMaxWidth()
                .height(height)
                .shapeBackground(shape, surfaceContainerProvider())
                .clickable(actionStartActivity(widgetIntent(context, row.date)))
                .semantics { testTag = "festivals_entry_$index" }
                .padding(start = 6.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                GlanceModifier
                    .size(if (height >= 46.dp) 36.dp else 30.dp)
                    .shapeBackground(
                        R.drawable.widget_shape_12,
                        if (row.isHoliday) holidayContainerProvider() else surfaceContainerHighestProvider(),
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = row.bsDay,
                style =
                    TextStyle(
                        color = if (row.isHoliday) onHolidayContainerProvider() else GlanceTheme.colors.onSurface,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                maxLines = 1,
            )
        }
        Text(
            text = row.name,
            modifier = GlanceModifier.defaultWeight().padding(horizontal = 12.dp),
            style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium),
            maxLines = 1,
        )
        Text(
            text = row.relative,
            style =
                TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                ),
            maxLines = 1,
        )
    }
}

@Composable
private fun EmptyText(
    text: String,
    ink: ColorProvider,
) {
    Box(modifier = GlanceModifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Text(text = text, style = TextStyle(color = ink, fontSize = 13.sp), maxLines = 3)
    }
}

private val COMPACT_HEIGHT = 100.dp
private val TALL_HEIGHT = 150.dp
private val ROOMY_LIST_HEIGHT = 250.dp
private val MEDIUM_LIST_HEIGHT = 190.dp
private val LIST_CHROME = 62.dp
private val ROW_GAP = 2.dp
