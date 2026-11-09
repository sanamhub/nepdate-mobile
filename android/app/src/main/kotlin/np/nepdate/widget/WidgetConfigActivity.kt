package np.nepdate.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import np.nepdate.NepdateApplication
import np.nepdate.R
import np.nepdate.ui.settings.Choice
import np.nepdate.ui.settings.ConnectedToggleGroup
import np.nepdate.ui.theme.NepdateTheme

/**
 * The configuration activity every widget offers: the background, and for the widgets that have
 * detail lines or controls, whether to show them (a minimal widget shows the date only).
 */
class WidgetConfigActivity : ComponentActivity() {
    private val appWidgetId: Int by lazy {
        intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
    }
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (!isOwnWidget(appWidgetId)) {
            finish()
            return
        }
        setResult(RESULT_CANCELED, resultIntent())
        val manager = GlanceAppWidgetManager(this)
        val glanceId = manager.getGlanceIdBy(appWidgetId)
        val provider =
            AppWidgetManager
                .getInstance(this)
                .getAppWidgetInfo(appWidgetId)
                ?.provider
                ?.className
                .orEmpty()
        val kind = WidgetKind.of(provider)
        setContent {
            NepdateTheme {
                WidgetConfigScreen(
                    glanceIdValue = glanceId,
                    kind = kind,
                    onCancel = { finish() },
                    onSave = { choice -> save(choice) },
                )
            }
        }
    }

    override fun onDestroy() {
        activityScope.cancel()
        super.onDestroy()
    }

    private fun save(choice: WidgetChoice) {
        val glanceId = GlanceAppWidgetManager(this).getGlanceIdBy(appWidgetId)
        activityScope.launch {
            updateAppWidgetState(this@WidgetConfigActivity, glanceId) { preferences ->
                preferences[WIDGET_OPACITY_KEY] = choice.opacity.key
                preferences[WIDGET_MINIMAL_KEY] = !choice.details
            }
            val container = (application as NepdateApplication).container
            container.widgetRefresh.refresh(container.settingsRepository.settings.first(), resetMonth = false)
            runOnUiThread {
                setResult(RESULT_OK, resultIntent())
                finish()
            }
        }
    }

    /**
     * The activity is exported so launchers can start it, so any app could pass an id. Only an id
     * bound to one of this app's providers is configured.
     */
    private fun isOwnWidget(id: Int): Boolean {
        if (id == AppWidgetManager.INVALID_APPWIDGET_ID) return false
        val provider = AppWidgetManager.getInstance(this).getAppWidgetInfo(id)?.provider
        return provider?.packageName == packageName
    }

    private fun resultIntent(): Intent = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
}

/** What one widget's configuration screen offers, from its provider class. */
internal data class WidgetKind(
    val defaultOpacity: WidgetOpacity,
    val hasDetails: Boolean,
) {
    companion object {
        private val WITH_DETAILS =
            setOf(
                TodayGlanceWidgetReceiver::class.java.name,
                UpcomingGlanceWidgetReceiver::class.java.name,
                NextFestivalGlanceWidgetReceiver::class.java.name,
                DateStripGlanceWidgetReceiver::class.java.name,
                WeekGlanceWidgetReceiver::class.java.name,
                TextDateGlanceWidgetReceiver::class.java.name,
            )

        fun of(providerClass: String): WidgetKind =
            WidgetKind(
                defaultOpacity =
                    if (providerClass == TextDateGlanceWidgetReceiver::class.java.name) {
                        TEXT_DATE_OPACITY
                    } else {
                        WidgetOpacity.SOLID
                    },
                hasDetails = providerClass in WITH_DETAILS,
            )
    }
}

/** The choices the configuration screen saves. */
internal data class WidgetChoice(
    val opacity: WidgetOpacity,
    val details: Boolean,
)

@Composable
private fun WidgetConfigScreen(
    glanceIdValue: androidx.glance.GlanceId,
    kind: WidgetKind,
    onCancel: () -> Unit,
    onSave: (WidgetChoice) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var choice by remember { mutableStateOf(WidgetChoice(kind.defaultOpacity, details = true)) }
    LaunchedEffect(glanceIdValue) {
        val preferences =
            getAppWidgetState<Preferences>(
                context,
                PreferencesGlanceStateDefinition,
                glanceIdValue,
            )
        choice = WidgetChoice(preferences.opacity(kind.defaultOpacity), details = !preferences.minimal())
    }
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                text = stringResource(R.string.widget_configuration_title),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = stringResource(R.string.widget_background_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            OpacityChoices(choice.opacity) { choice = choice.copy(opacity = it) }
            if (kind.hasDetails) {
                DetailsSwitch(choice.details) { choice = choice.copy(details = it) }
            }
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onCancel) {
                    Text(stringResource(R.string.widget_cancel))
                }
                Button(
                    onClick = { onSave(choice) },
                    modifier = Modifier.padding(start = 8.dp).testTag("widget_config_save"),
                ) {
                    Text(stringResource(R.string.widget_save))
                }
            }
        }
    }
}

/** The background as a connected button group: one tap, every option visible at once. */
@Composable
private fun OpacityChoices(
    opacity: WidgetOpacity,
    onSelect: (WidgetOpacity) -> Unit,
) {
    ConnectedToggleGroup(
        options = WidgetOpacity.entries.map { Choice(it, stringResource(it.label)) },
        selected = opacity,
        onSelect = onSelect,
        haptics = null,
        tags = WidgetOpacity.entries.map { "widget_opacity_${it.key}" },
    )
}

@Composable
private fun DetailsSwitch(
    details: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .toggleable(value = details, role = Role.Switch, onValueChange = onChange)
                .padding(vertical = 8.dp)
                .testTag("widget_details"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(stringResource(R.string.widget_details_title), style = MaterialTheme.typography.bodyLarge)
            Text(
                stringResource(R.string.widget_details_summary),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = details, onCheckedChange = null)
    }
}

private val WidgetOpacity.label: Int
    get() =
        when (this) {
            WidgetOpacity.SOLID -> R.string.widget_opacity_solid
            WidgetOpacity.SEVENTY -> R.string.widget_opacity_seventy
            WidgetOpacity.TRANSPARENT -> R.string.widget_opacity_transparent
        }
