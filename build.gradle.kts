import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    id("org.jetbrains.dokka") version "1.7.10"
    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
}

buildscript {
    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.6.10"))
    }
}

allprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    group = "eu.joekoe.auto-context"
    version = "0.0.1"

    ktlint {
        version.set("0.45.2")
        filter {
            exclude("**/generated/**")
        }
    }

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm") // kotlin("jvm")

    tasks {
        withType<KotlinCompile> {
            // allow warnings in demo for easier KSP logging
            kotlinOptions.allWarningsAsErrors = project.name != "demo"
        }

        withType<Test> {
            testLogging {
                showExceptions = false
                showCauses = true
                showStackTraces = true
                exceptionFormat = FULL
            }
        }
    }

    dependencies {
        val kotest = "5.4.2"
        val junit = "5.9.0"
        val mockkVersion = "1.12.5"

        implementation(kotlin("stdlib"))

        implementation("org.slf4j:slf4j-api:2.0.0")

        testImplementation("ch.qos.logback:logback-classic:1.2.11")

        testImplementation("io.kotest:kotest-assertions-core:$kotest")
        testImplementation("io.kotest:kotest-runner-junit5:$kotest")

        testImplementation("org.junit.jupiter:junit-jupiter-api:$junit")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit")

        testImplementation("io.mockk:mockk:$mockkVersion")
    }

    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.dokka")

    val sourcesJar by tasks.creating(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }

    val javadocJar by tasks.creating(Jar::class) {
        dependsOn("dokkaJavadoc")
        archiveClassifier.set("javadoc")
        from(File(buildDir, "javadoc"))
    }

    extensions.configure<PublishingExtension>("publishing") {
        fun isRelease() = project.hasProperty("release")

        publications {
            register("maven", MavenPublication::class) {
                from(components["kotlin"])
                artifact(sourcesJar)
                artifact(javadocJar)
                artifactId = project.name
                version = project.version.toString() + if (isRelease()) "" else "-SNAPSHOT"
            }
        }
    }
}
