package np.nepdate

import android.content.Intent
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        // The manifest gives this activity Theme.Nepdate.Launch so Android 8 to 11 show the icon
        // while the process starts; swap to the app theme before any view is made.
        setTheme(R.style.Theme_Nepdate)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        applyDeepLink(intent)
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
