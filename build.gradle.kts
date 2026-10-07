plugins {
    kotlin("jvm") version "2.4.10"
    kotlin("plugin.serialization") version "2.4.10"

    application
}

application {
    mainClass.set("gamefit.ui.MainKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-client-core:3.6.0")
    implementation("io.ktor:ktor-client-cio:3.6.0")
    implementation("io.ktor:ktor-client-content-negotiation:3.6.0")
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.6.0")
}

sourceSets {
    main {
        kotlin.srcDirs("src")
    }
}
