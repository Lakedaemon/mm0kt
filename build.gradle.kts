import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.31"//"1.5.0-M2"
    application
}

group = "org.mm0.kt"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    val kotlin_version = "1.4.31"//"1.5.0-M2"
    val spek_version="2.0.15"//"2.0.16"//
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    implementation("org.jetbrains.kotlin:kotlin-test")
    // some version of Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")

    testImplementation("org.spekframework.spek2:spek-dsl-jvm:$spek_version")
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:$spek_version")

    // spek requires kotlin-reflect, can be omitted if already in the classpath
    testRuntimeOnly("org.jetbrains.kotlin:kotlin-reflect:$kotlin_version")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClassName = "MainKt"
}