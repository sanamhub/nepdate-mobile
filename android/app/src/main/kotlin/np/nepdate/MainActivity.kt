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
import io.github.sanamhub.nepdate.NepaliDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import np.nepdate.ui.theme.NepdateTheme
import np.nepdate.widget.WidgetPreviewPublisher
import np.nepdate.widget.dateFromIntent

class MainActivity : ComponentActivity() {
    private var deepLinkDate by mutableStateOf<NepaliDate?>(null)
    private var deepLinkKey by mutableIntStateOf(0)
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        applyDeepLink(intent)
        val container = (application as NepdateApplication).container
        setContent {
            NepdateTheme {
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
