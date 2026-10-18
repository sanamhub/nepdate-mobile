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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
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
import np.nepdate.ui.theme.NepdateTheme

/** The configuration activity every widget offers: one background-opacity choice (A2-04). */
class WidgetConfigActivity : ComponentActivity() {
    private val appWidgetId: Int by lazy {
        intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
    }
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        setResult(RESULT_CANCELED, resultIntent())
        val manager = GlanceAppWidgetManager(this)
        val glanceId = manager.getGlanceIdBy(appWidgetId)
        setContent {
            NepdateTheme {
                WidgetConfigScreen(
                    glanceIdValue = glanceId,
                    onCancel = { finish() },
                    onSave = { opacity -> save(opacity) },
                )
            }
        }
    }

    override fun onDestroy() {
        activityScope.cancel()
        super.onDestroy()
    }

    private fun save(opacity: WidgetOpacity) {
        val glanceId = GlanceAppWidgetManager(this).getGlanceIdBy(appWidgetId)
        activityScope.launch {
            updateAppWidgetState(this@WidgetConfigActivity, glanceId) { preferences ->
                preferences[WIDGET_OPACITY_KEY] = opacity.key
            }
            val container = (application as NepdateApplication).container
            container.widgetRefresh.refresh(container.settingsRepository.settings.first(), resetMonth = false)
            runOnUiThread {
                setResult(RESULT_OK, resultIntent())
                finish()
            }
        }
    }

    private fun resultIntent(): Intent = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
}

@Composable
private fun WidgetConfigScreen(
    glanceIdValue: androidx.glance.GlanceId,
    onCancel: () -> Unit,
    onSave: (WidgetOpacity) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var opacity by remember { mutableStateOf(WidgetOpacity.SOLID) }
    LaunchedEffect(glanceIdValue) {
        val preferences =
            getAppWidgetState<Preferences>(
                context,
                PreferencesGlanceStateDefinition,
                glanceIdValue,
            )
        opacity = WidgetOpacity.fromKey(preferences[WIDGET_OPACITY_KEY])
    }
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.widget_configuration_title),
                style = MaterialTheme.typography.headlineSmall,
            )
            OpacityChoices(opacity) { opacity = it }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onCancel) {
                    Text(stringResource(R.string.widget_cancel))
                }
                Button(
                    onClick = { onSave(opacity) },
                    modifier = Modifier.padding(start = 8.dp).testTag("widget_config_save"),
                ) {
                    Text(stringResource(R.string.widget_save))
                }
            }
        }
    }
}

@Composable
private fun OpacityChoices(
    opacity: WidgetOpacity,
    onSelect: (WidgetOpacity) -> Unit,
) {
    Column(modifier = Modifier.selectableGroup()) {
        WidgetOpacity.entries.forEach { option ->
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = opacity == option,
                            onClick = { onSelect(option) },
                        ).padding(vertical = 8.dp)
                        .testTag("widget_opacity_${option.key}"),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = opacity == option, onClick = { onSelect(option) })
                Text(
                    text = stringResource(option.label),
                    modifier = Modifier.padding(start = 12.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

private val WidgetOpacity.label: Int
    get() =
        when (this) {
            WidgetOpacity.SOLID -> R.string.widget_opacity_solid
            WidgetOpacity.SEVENTY -> R.string.widget_opacity_seventy
            WidgetOpacity.TRANSPARENT -> R.string.widget_opacity_transparent
        }
