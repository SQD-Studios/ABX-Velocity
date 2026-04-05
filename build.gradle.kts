plugins {
    id("java")
    id("eclipse")
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.8"
    id("com.gradleup.shadow") version "9.3.1"
    id("io.papermc.hangar-publish-plugin") version "0.1.2"
}

group = "net.chamosmp"
version = "1.2.1"

repositories {
    mavenCentral()
    maven("https://repo.extendedclip.com/releases/")
}

dependencies {
    compileOnly("me.clip:placeholderapi:2.12.2")
    implementation(project(":velocity"))
    implementation(project(":paper"))
}

val targetJavaVersion = 21
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}

// ---------------------- Template Generation ----------------------
val templateSource = file("src/main/templates")
val templateDest = layout.buildDirectory.dir("generated/sources/templates")

val generateTemplates = tasks.register("generateTemplates") {
    doLast {
        copy {
            from(templateSource)
            into(templateDest.get().asFile)
            expand("version" to project.version.toString())
        }
    }
}

tasks.named("compileJava") {
    dependsOn(generateTemplates)
}
sourceSets["main"].java.srcDir(templateDest)

// ---------------------- ShadowJar ----------------------
tasks.named("shadowJar") {
    doFirst {
        // runtimeClasspath already included by default in ShadowJar
        // duplicatesStrategy and relocate are not available in Kotlin DSL for com.gradleup.shadow
        println("ShadowJar task ready to run")
    }
}

