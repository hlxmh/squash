val releaseVersion: String by project
val kotlinVersion: String by project

kotlin {
    jvmToolchain(17)
}

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "org.example"
version = releaseVersion

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    testImplementation(kotlin("test"))

//    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    implementation(project(":server"))}

tasks.test {
    useJUnitPlatform()
}