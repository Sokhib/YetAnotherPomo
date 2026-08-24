// Top-level build file. Plugins are declared here with `apply false` purely to pin ONE version
// for the whole build; each module then applies the ones it actually needs.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.kmp.native.coroutines) apply false
}
