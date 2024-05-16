import org.jetbrains.compose.desktop.application.dsl.TargetFormat

val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project
val slf4jVersion: String by project
val releaseVersion: String by project

kotlin {
    jvmToolchain(17)
}

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

group = "com.example"
version = releaseVersion

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("org.slf4j:slf4j-simple:$slf4jVersion")
    implementation("dev.snipme:kodeview:0.8.0")
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("com.google.cloud.sql:mysql-socket-factory-connector-j-8:1.2.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")
    implementation("io.github.cdimascio:java-dotenv:5.2.2")
    //test libraries
//    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    implementation(project(":models"))
    implementation(project(":server"))
    implementation(project(":websocket"))
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            includeAllModules = true
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe)
            packageName = "cs346-project"
            packageVersion = releaseVersion
        }
    }
}
