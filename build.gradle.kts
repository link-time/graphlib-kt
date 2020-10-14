group = "com.linked-planet.lib"
version = "0.0.1"

ext.set("kotlinVersion", "1.3.50-eap-54")
ext.set("jvmTarget", "1.8")

val kotlinVersion: String by project
val jvmTarget: String by project

plugins {
    kotlin("jvm") version "1.3.50-eap-54"
}

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
    maven { url = uri("https://dl.bintray.com/kotlin/kotlin-eap") }
}

dependencies {
    implementation(kotlin("stdlib-jdk8", version = kotlinVersion))
    implementation(group = "ch.qos.logback", name = "logback-classic", version = "1.2.3")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
