pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "nepdate-android"

include(":app", ":baselineprofile")

// ADR-0003 §6: nepdate-kotlin is a local checkout until its first release. A developer keeps it
// beside the repository; CI checks it out inside the workspace. The property overrides both.
val defaultLibraryPath =
    listOf("../../nepdate-kotlin", "../nepdate-kotlin")
        .firstOrNull { file(it).isDirectory }
        ?: "../../nepdate-kotlin"
val libraryPath = providers.gradleProperty("nepdateKotlinPath").getOrElse(defaultLibraryPath)

includeBuild(libraryPath) {
    dependencySubstitution {
        substitute(module("io.github.sanamhub:nepdate")).using(project(":nepdate"))
        substitute(module("io.github.sanamhub:nepdate-patro")).using(project(":nepdate-patro"))
    }
}
