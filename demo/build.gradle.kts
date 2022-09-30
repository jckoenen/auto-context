plugins {
    id("com.google.devtools.ksp") version "1.7.10-1.0.6"
}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
}

dependencies {
    implementation(projects.api)
    ksp(projects.processor)
    api(platform("io.arrow-kt:arrow-stack:1.1.2"))

    api("io.arrow-kt:arrow-core")
    api("io.arrow-kt:arrow-fx-coroutines")
    api("io.arrow-kt:arrow-fx-stm")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
}
