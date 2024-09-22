plugins {
    kotlin("jvm") version "2.0.0"
}

group = "io.tebex"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(project(":sdk"))
    implementation("com.google.guava:guava:32.1.2-jre")
    compileOnly("net.minestom:minestom-snapshots:4305006e6b")
    compileOnly("gg.airbrush:server:0.3.2")
    compileOnly("dev.dejvokep:boosted-yaml:1.3")
}

kotlin {
    jvmToolchain(21)
}