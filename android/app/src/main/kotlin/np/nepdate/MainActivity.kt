package np.nepdate

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.LocaleList
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.sanamhub.nepdate.NepaliDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import np.nepdate.data.Settings
import np.nepdate.ui.theme.NepdateTheme
import np.nepdate.ui.theme.isDark
import np.nepdate.widget.WidgetPreviewPublisher
import np.nepdate.widget.dateFromIntent

class MainActivity : ComponentActivity() {
    private var deepLinkDate by mutableStateOf<NepaliDate?>(null)
    private var deepLinkKey by mutableIntStateOf(0)
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var drawnLocales: LocaleList? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // The manifest gives this activity Theme.Nepdate.Launch so Android 8 to 11 show the icon
        // while the process starts; swap to the app theme before any view is made.
        setTheme(R.style.Theme_Nepdate)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // A recreated activity (rotation, theme change) keeps the launch intent; reading it again
        // would reopen the day sheet the user already closed.
        if (savedInstanceState == null) applyDeepLink(intent)
        val container = (application as NepdateApplication).container
        setContent {
            val settings by container.settingsRepository.settings.collectAsStateWithLifecycle(
                initialValue = Settings(),
            )
            NepdateTheme(
                darkTheme = settings.look.isDark(),
                dynamicColor = settings.wallpaperColors,
            ) {
                NepdateApp(
                    container = container,
                    initialDate = deepLinkDate,
                    initialDateKey = deepLinkKey,
                    onFirstGridDrawn = { reportFullyDrawn() },
                )
            }
        }
        refreshWidgets()
    }

    /**
     * The manifest keeps this activity alive through a language change (first run or Settings), so
     * the widgets and their previews are redrawn here in the new language instead of on recreation.
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // The activity's resources already carry the new configuration here, so compare with the
        // locales this activity last drew the widgets for.
        if (newConfig.locales != drawnLocales) refreshWidgets()
    }

    private fun refreshWidgets() {
        drawnLocales = resources.configuration.locales
        val container = (application as NepdateApplication).container
        activityScope.launch {
            runCatching {
                container.widgetRefresh.refresh(
                    container.settingsRepository.settings.first(),
                    resetMonth = false,
                )
            }
            WidgetPreviewPublisher(applicationContext).publish()
        }
    }

    override fun onDestroy() {
        activityScope.cancel()
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        applyDeepLink(intent)
    }

    private fun applyDeepLink(intent: Intent?) {
        deepLinkDate = dateFromIntent(intent)
        deepLinkKey++
    }
}
