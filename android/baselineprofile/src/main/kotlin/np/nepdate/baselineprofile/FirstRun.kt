package np.nepdate.baselineprofile

import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertTrue

/** The button that leaves the first-run welcome. */
private const val FIRST_RUN_OPEN = "Open the patro"

/** The first tab, which the navigation bar shows once the welcome is done. */
private const val FIRST_TAB = "Festivals"

private const val FIRST_RUN_TIMEOUT_MS = 5_000L
private const val UI_TIMEOUT_MS = 5_000L

/**
 * A fresh install shows the first-run welcome before the tabs. Open it, so a test starts on the
 * tabs; the app keeps the choice, so a later launch starts on the tabs and the wait runs out.
 */
internal fun UiDevice.openPastFirstRun() {
    val open = By.text(FIRST_RUN_OPEN).pkg(PACKAGE_NAME)
    val button = wait(Until.findObject(open), FIRST_RUN_TIMEOUT_MS) ?: return
    button.click()
    val tabs = wait(Until.hasObject(By.desc(FIRST_TAB).pkg(PACKAGE_NAME)), UI_TIMEOUT_MS)
    assertTrue("The first-run screen did not open the tabs", tabs)
}

/**
 * Opens the app once to leave the first-run welcome, then stops it, so a measured start begins on
 * the month. A managed device with no app data otherwise measures the welcome screen.
 */
internal fun UiDevice.prepareForMeasuredLaunch() {
    executeShellCommand("am start -n $PACKAGE_NAME/.MainActivity")
    openPastFirstRun()
    executeShellCommand("am force-stop $PACKAGE_NAME")
}
