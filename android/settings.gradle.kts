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

// ADR-0003 §6: nepdate-kotlin is a sibling checkout until its first release. CI checks out the
// pinned commit beside the repository, so the same relative path works there.
includeBuild("../../nepdate-kotlin") {
    dependencySubstitution {
        substitute(module("io.github.sanamhub:nepdate")).using(project(":nepdate"))
        substitute(module("io.github.sanamhub:nepdate-patro")).using(project(":nepdate-patro"))
    }
}
