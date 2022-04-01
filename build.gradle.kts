
plugins {
    kotlin("jvm") version "1.6.10"
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
    compileOnly("javax.servlet:javax.servlet-api:4.0.1")
    compileOnly("javax.servlet.jsp:javax.servlet.jsp-api:2.3.3")
    testImplementation(kotlin("test"))
    testImplementation("com.h2database:h2:2.1.210")
}

tasks {
    test {
        useJUnitPlatform()
    }
}
