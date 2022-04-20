
plugins {
    kotlin("jvm") version "1.6.20"
}

group = "org.geezer"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

configurations {
    testImplementation {
        extendsFrom(configurations.compileOnly.get())
    }
}

dependencies {
    implementation(platform("io.arrow-kt:arrow-stack:1.0.1"))
    implementation("io.arrow-kt:arrow-core")
    implementation("commons-codec:commons-codec:1.15")
    implementation("commons-io:commons-io:2.11.0")
    compileOnly("org.jetbrains.kotlin:kotlin-reflect:1.6.10")
    compileOnly("jakarta.servlet:jakarta.servlet-api:5.0.0")
    compileOnly("jakarta.servlet.jsp:jakarta.servlet.jsp-api:3.0.0")
    testImplementation(kotlin("test"))
}

tasks.test { useJUnitPlatform() }

