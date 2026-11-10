// settings.gradle.kts
pluginManagement {
    repositories {
        // Repositories containing Google/Android/Firebase plugins
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()        // Firebase SDK, AndroidX, Material
        mavenCentral()  // Glide
    }
}

rootProject.name = "VLU_Lab10_FirebaseAuth_Glide_2474802010210"
include(":app")
