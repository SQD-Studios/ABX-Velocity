import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.named

plugins {
    id("java")
    id("com.gradleup.shadow") version "9.3.1"
}

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net")
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven("https://repo.extendedclip.com/releases/")
}


dependencies {
    compileOnly("commons-io:commons-io:2.18.0")
    compileOnly("org.apache.commons:commons-lang3:3.17.0")
    compileOnly("org.hsqldb:hsqldb:2.7.3")
    testImplementation(platform("org.junit:junit-bom:6.0.3"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
    compileOnly("org.slf4j:slf4j-api:2.0.16")
    compileOnly("org.slf4j:slf4j-nop:2.0.13")
    implementation("com.zaxxer:HikariCP:6.2.1")
    compileOnly("com.google.code.gson:gson:2.11.0")
    compileOnly("net.kyori:adventure-text-minimessage:4.17.0")
    compileOnly("net.kyori:adventure-text-serializer-ansi:4.17.0")
    implementation("net.kyori:adventure-text-minimessage:4.26.1")
// Source: https://mvnrepository.com/artifact/com.mysql/mysql-connector-j
    implementation("com.mysql:mysql-connector-j:9.6.0")
    compileOnly("net.luckperms:api:5.4")
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.12.2")
}

val targetJavaVersion = 21
java {
    toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}
tasks.named<ShadowJar>("shadowJar") {
    configurations = listOf(project.configurations.runtimeClasspath.get())
    relocate("org.bstats", project.group.toString())
}