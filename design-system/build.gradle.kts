import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
}

// Intentionally a plain Kotlin/JVM library: no Android Gradle plugin, no `android {}` block,
// no resources and no dependencies. The token set describes the Organic design language in
// toolkit-free primitives so it can be dropped into any consumer (Compose, View system,
// desktop, a screenshot tool) by writing a small binding layer against it.
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

dependencies {
    testImplementation(libs.junit)
}

tasks.withType<Test>().configureEach {
    useJUnit()
}
