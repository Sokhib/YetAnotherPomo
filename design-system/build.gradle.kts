import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
}

// Still intentionally dependency-free: no Compose, no Android APIs, no third-party libraries. It
// only changed shape - what was a plain Kotlin/JVM jar is now a multiplatform module, because a
// JVM jar cannot be consumed by Kotlin/Native. The token set is unchanged; the point was always
// that a consumer binds it to its own toolkit, and now iOS can be one of those consumers.
kotlin {
    android {
        namespace = "com.tora.organic"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }

        withHostTest {}
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
