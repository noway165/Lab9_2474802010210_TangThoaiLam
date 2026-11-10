// build.gradle.kts (Project level)
// Declares plugin versions for the whole project; "apply false" only loads the version,
// activation happens in the app module.
plugins {
    // Android Gradle Plugin
    id("com.android.application") version "8.5.2" apply false
    // Kotlin support plugin for Android
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    // Google Services Plugin: reads google-services.json and generates Firebase config resources
    id("com.google.gms.google-services") version "4.4.2" apply false
    // KSP: annotation processor runner for the Glide compiler (must match the Kotlin version)
    id("com.google.devtools.ksp") version "1.9.24-1.0.20" apply false
}
