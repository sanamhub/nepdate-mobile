plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.baselineprofile) apply false
    // AGP 9 supplies Kotlin itself. The plugin is on the classpath, not applied, so Gradle uses
    // Kotlin 2.4.20 instead of the 2.2.10 that AGP depends on.
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.roborazzi) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.play.publisher) apply false
}

// ADR-0008: the gates that apply before the app has screens or widgets.
tasks.register("ci") {
    group = "verification"
    description = "Runs the Android quality gates."
    dependsOn(
        ":app:ktlintCheck",
        ":app:detekt",
        ":app:lintDebug",
        ":app:testDebugUnitTest",
        ":app:verifyRoborazziDebug",
        ":app:checkNoInternet",
        ":app:checkPermissions",
        ":app:checkWidgetBitmaps",
        ":app:checkNoWorkManagerUse",
        ":app:checkBaselineProfile",
        ":baselineprofile:ktlintCheck",
        ":baselineprofile:detekt",
        ":baselineprofile:assemble",
    )
}
