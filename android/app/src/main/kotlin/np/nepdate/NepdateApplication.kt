package np.nepdate

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import np.nepdate.di.AppContainer
import np.nepdate.schedule.SettingsWidgetSync

/** Owns the manual dependency graph for the process (ADR-0005). */
class NepdateApplication : Application() {
    /** The app's singletons, created on first use. */
    val container: AppContainer by lazy { AppContainer(this) }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        SettingsWidgetSync(container.settingsRepository.settings) { settings ->
            container.widgetRefresh.refresh(settings, resetMonth = false)
        }.start(appScope)
    }
}
