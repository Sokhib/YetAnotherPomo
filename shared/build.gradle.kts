import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    // Replaces `kotlin("android")` / `kotlin("jvm")`. This is what turns `src/` into a source-set
    // TREE instead of a single folder, and what gives us the `kotlin { }` block below.
    alias(libs.plugins.kotlin.multiplatform)
    // NOT `com.android.library`. This variant knows how to slot an Android target INTO the
    // `kotlin { }` block rather than owning a top-level `android { }` block of its own.
    alias(libs.plugins.android.kotlin.multiplatform.library)
    // Generates Swift-friendly wrappers for suspend functions and Flows. Without it, the
    // Objective-C header exports StateFlow<FocusUiState> as an untyped StateFlow that Swift
    // cannot collect at all.
    alias(libs.plugins.kmp.native.coroutines)
}

kotlin {
    // ---- Target 1: Android. Produces the .aar that :app will depend on. --------------------
    // Note this `android { }` sits INSIDE `kotlin { }` - it is a Kotlin target, not the
    // top-level `android { }` block your :app module has. (AGP 9.1 spelled this `androidLibrary`;
    // 9.3 renamed it. Template code you copy from the internet will often still say the old name.)
    android {
        // A library has no applicationId. `namespace` is what its generated R class lands in,
        // and it must NOT collide with :app's namespace.
        namespace = "com.tora.yetanotherpomo.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }

        // Opts the Android target into running commonTest on the local JVM. Without this the
        // Android side silently skips the shared tests and only iOS would run them.
        withHostTest {}
    }

    // ---- Targets 2 & 3: iOS. -----------------------------------------------------------------
    // iosArm64          = real iPhones.
    // iosSimulatorArm64 = the simulator on Apple-silicon Macs.
    // They are separate CPU architectures, so Kotlin/Native compiles each one separately - but
    // both read the SAME src/iosMain folder, which the plugin wires up as their shared parent.
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            // Xcode will `import Shared`. This is that name.
            baseName = "Shared"
            // Static = linked into the app binary. Simpler than a dynamic framework; no
            // "embed and sign" build phase to get wrong.
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Flow/StateFlow/coroutines are multiplatform, so FocusRepository's `Flow<...>`
            // signatures compile unchanged for Android AND iOS. This is the whole reason the
            // domain layer ports for free.
            api(libs.kotlinx.coroutines.core)
            // `api`, not `implementation`: DataStore<Preferences> appears in FocusRepositoryImpl's
            // constructor, so anything that constructs one needs the type visible.
            api(libs.androidx.datastore.preferences.core)
            // DataStore's multiplatform file API speaks okio.Path rather than java.io.File.
            implementation(libs.okio)
            // `api` so :app and the iOS framework can both declare their own Koin modules
            // against the same Koin runtime.
            api(libs.koin.core)
            // FocusViewModel extends androidx's ViewModel in COMMON code - the artifact has
            // shipped iOS targets since 2.8, so no third-party ViewModel library is needed.
            api(libs.androidx.lifecycle.viewmodel)
            // A ViewModel usable from common code that SwiftUI can treat as an ObservableObject.
            // On Android it still subclasses androidx's ViewModel, so nothing there changes.
            api(libs.kmp.observable.viewmodel)
        }

        commonTest.dependencies {
            // The multiplatform test framework. `kotlin("test")` resolves to JUnit on Android and
            // to Kotlin/Native's own test runner on iOS - one test file, run on every target.
            implementation(kotlin("test"))
        }

        // Required by KMP-ObservableViewModel.
        all {
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
            languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
        }

        androidMain.dependencies {
            // Android-only DataStore artifact, pulled in for exactly one function:
            // Context.preferencesDataStoreFile(). See FocusDataStore.android.kt for why.
            implementation(libs.androidx.datastore.preferences)
            // Only for androidContext() - the Android module needs a Context to locate filesDir.
            implementation(libs.koin.android)
        }
    }
}
