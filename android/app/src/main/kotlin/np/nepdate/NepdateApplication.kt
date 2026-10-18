package np.nepdate

import android.app.Application
import np.nepdate.di.AppContainer

/** Owns the manual dependency graph for the process (ADR-0005). */
class NepdateApplication : Application() {
    /** The app's singletons, created on first use. */
    val container: AppContainer by lazy { AppContainer(this) }
}
