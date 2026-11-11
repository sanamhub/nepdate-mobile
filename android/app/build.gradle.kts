import com.android.build.api.artifact.SingleArtifact
import org.w3c.dom.Element

val releaseStatusFile = layout.buildDirectory.file("release-data-status.txt")

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.baselineprofile)
    alias(libs.plugins.roborazzi)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    alias(libs.plugins.play.publisher)
}

android {
    namespace = "np.nepdate"
    compileSdk = 37

    defaultConfig {
        applicationId = "np.nepdate"
        minSdk = 26
        targetSdk = 37
        // Tester builds pass -PversionCode=N so each one installs over the last as an update.
        versionCode = providers.gradleProperty("versionCode").orNull?.toInt() ?: 10
        versionName = "1.0.0-beta.1"
        buildConfigField("String", "NEPDATE_VERSION", "\"${libs.versions.nepdate.get()}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    // The app speaks English and Nepali only. Without this filter every AndroidX and Material
    // library ships its strings in some 80 languages that the app can never show.
    androidResources {
        localeFilters += listOf("en", "ne")
    }

    // The production environment provides these variables. Without them the release build
    // stays unsigned, which is what the reproducible-build job compares.
    signingConfigs {
        val keystore = providers.environmentVariable("ANDROID_KEYSTORE_PATH").orNull
        val keystorePassword = providers.environmentVariable("ANDROID_KEYSTORE_PASSWORD").orNull
        val keyAlias = providers.environmentVariable("ANDROID_KEY_ALIAS").orNull
        val keyPassword = providers.environmentVariable("ANDROID_KEY_PASSWORD").orNull
        if (keystore != null && keystorePassword != null && keyAlias != null && keyPassword != null) {
            create("release") {
                storeFile = file(keystore)
                storePassword = keystorePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            // No commit hash or build timestamp in the APK.
            vcsInfo {
                include = false
            }
            // PNG optimisation must not depend on the host or a timestamp.
            isCrunchPngs = false
            signingConfigs.findByName("release")?.let { signingConfig = it }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    androidResources {
        generateLocaleConfig = true
    }

    // The first-run and settings language choice switch between Nepali and English at runtime,
    // so every install needs both languages rather than only the device's.
    bundle {
        language {
            enableSplit = false
        }
    }

    lint {
        warningsAsErrors = true
        abortOnError = true
        checkReleaseBuilds = false
        // Versions are exact pins (ADR-0003 §1, ADR-0005); Dependabot proposes upgrades.
        // Adaptive icons stay in -v26: AAPT ignores the tag outside a v26 folder even at minSdk 26.
        disable +=
            setOf(
                "AndroidGradlePluginVersion",
                "GradleDependency",
                "NewerVersionAvailable",
                "ObsoleteSdkInt",
            )
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.systemProperties["robolectric.pixelCopyRenderMode"] = "hardware"
                it.systemProperty("releaseStatusFile", releaseStatusFile.get().asFile.absolutePath)
                it.inputs.property("releaseDate", providers.environmentVariable("RELEASE_DATE").orElse(""))
                it.maxHeapSize = "2g"
                // Half the cores keeps memory bounded on a 2g heap per fork. Each fork gets its
                // own Robolectric sandbox; the screenshot tests write one file per test into the
                // shared output directory, and only DataFreshnessTest writes releaseStatusFile,
                // so the forks share no writable state.
                it.maxParallelForks =
                    (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
            }
        }
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

// A human promotes from the internal track after checking the draft release.
play {
    defaultToAppBundles.set(true)
    track.set("internal")
}

// Widgets must not carry bitmaps. Colours, numerals and marks are text, shapes or vector
// drawables. A drawable loaded by resource id (ImageProvider(R.drawable.x)) is fine: the launcher
// inflates it. A bitmap, an Icon or a content Uri travels inside the RemoteViews parcel, which has
// a size limit, and draws blurred when scaled, so those are what this check refuses.
tasks.register("checkWidgetBitmaps") {
    group = "verification"
    description = "Fails when a widget source uses a Bitmap, an Icon or a Uri image instead of a drawable resource."
    inputs.files(fileTree("src/main/kotlin/np/nepdate/widget") { include("**/*.kt") })
    doLast {
        val forbidden =
            listOf(
                Regex("""\bBitmap\b"""),
                Regex("""\bcreateBitmap\b|\btoBitmap\b"""),
                Regex("""import android\.graphics\.drawable\.Icon\b"""),
                Regex("""\bIcon\.createWith"""),
                Regex("""import android\.net\.Uri\b"""),
            )
        val offending =
            inputs.files.files.filter { file ->
                val source = file.readText()
                forbidden.any { it.containsMatchIn(source) }
            }
        if (offending.isNotEmpty()) {
            throw GradleException(
                "checkWidgetBitmaps: bitmap, Icon or Uri image under widget/: " +
                    offending.joinToString(", ") { it.name },
            )
        }
    }
}

// ADR-0011: WorkManager is present only because Glance depends on it. App code must not use it.
tasks.register("checkNoWorkManagerUse") {
    group = "verification"
    description = "Fails when app source imports androidx.work (ADR-0011)."
    inputs.files(fileTree("src") { include("**/*.kt") })
    doLast {
        val offending = inputs.files.files.filter { file -> "import androidx.work" in file.readText() }
        if (offending.isNotEmpty()) {
            throw GradleException(
                "checkNoWorkManagerUse: androidx.work imported in " +
                    offending.joinToString(", ") { it.name },
            )
        }
    }
}

// ADR-0007: the merged release manifest must not request network access, and no permission may
// appear beyond the allow-list. Registered for the release variant because that is what ships.
androidComponents {
    onVariants(selector().withBuildType("release")) { variant ->
        val mergedManifest = variant.artifacts.get(SingleArtifact.MERGED_MANIFEST)
        val manifestFile = mergedManifest.map { it.asFile }
        val applicationId = variant.applicationId.get()

        tasks.register("checkNoInternet") {
            group = "verification"
            description = "Fails when the merged release manifest requests INTERNET."
            dependsOn(mergedManifest)
            inputs.file(manifestFile)
            doLast {
                val factory =
                    javax.xml.parsers.DocumentBuilderFactory
                        .newInstance()
                factory.isNamespaceAware = true
                val document = factory.newDocumentBuilder().parse(manifestFile.get())
                val nodes = document.getElementsByTagName("uses-permission")
                val permissions =
                    (0 until nodes.length)
                        .map { index ->
                            (nodes.item(index) as Element).getAttribute("android:name")
                        }.toSet()
                if ("android.permission.INTERNET" in permissions) {
                    throw GradleException(
                        "checkNoInternet: the merged release manifest requests " +
                            "android.permission.INTERNET.",
                    )
                }
            }
        }

        tasks.register("checkPermissions") {
            group = "verification"
            description = "Fails when the merged release manifest requests a permission outside the allow-list."
            dependsOn(mergedManifest)
            inputs.file(manifestFile)
            inputs.property("applicationId", applicationId)
            doLast {
                val factory =
                    javax.xml.parsers.DocumentBuilderFactory
                        .newInstance()
                factory.isNamespaceAware = true
                val document = factory.newDocumentBuilder().parse(manifestFile.get())

                val requested =
                    listOf("uses-permission", "uses-permission-sdk-23")
                        .flatMap { tag ->
                            val nodes = document.getElementsByTagName(tag)
                            (0 until nodes.length).map { index ->
                                (nodes.item(index) as Element).getAttribute("android:name")
                            }
                        }.toSet()

                // Exactly three entries are allowed: the boot receiver for the widget alarm, the
                // opt-in status bar date's notification permission (ADR-0013), and the app's own
                // AndroidX signature permission for dynamic receivers. A declared permission gets
                // no exemption; if it is also requested, it fails.
                val allowed =
                    setOf(
                        "android.permission.RECEIVE_BOOT_COMPLETED",
                        "android.permission.POST_NOTIFICATIONS",
                        "$applicationId.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION",
                    )
                val extra = requested - allowed
                if (extra.isNotEmpty()) {
                    throw GradleException(
                        "checkPermissions: permissions outside the allow-list: " +
                            extra.sorted().joinToString(", "),
                    )
                }
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.nepdate.core)
    implementation(libs.nepdate.patro)

    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.junit)
    testImplementation(libs.robolectric)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.androidx.glance.appwidget.testing)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    "baselineProfile"(project(":baselineprofile"))
}

// The generated profile is checked in and merged into every variant (ADR-0005).
baselineProfile {
    mergeIntoMain = true
    saveInSrc = true
    automaticGenerationDuringBuild = false
}

// ADR-0008: Roborazzi images are reviewed in the pull request.
roborazzi {
    outputDir.set(file("src/test/screenshots"))
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
}
