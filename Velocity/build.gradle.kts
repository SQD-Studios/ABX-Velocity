plugins {
    java
    eclipse
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.8"
    id("xyz.jpenilla.run-velocity") version "3.0.2"
    id("com.gradleup.shadow") version "9.3.1"
}

group = "net.chamosmp"

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "md_5-snapshots"
        url = uri("https://repo.md-5.net/content/repositories/snapshots/")
    }
    maven {
        name = "md_5-releases"
        url = uri("https://repo.md-5.net/content/repositories/releases/")
    }
    maven {
        name = "sonatype-snapshots"
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }
}

dependencies {
    implementation(project(":core"))
    implementation("org.yaml:snakeyaml:2.2")
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    compileOnly("net.luckperms:api:5.2")
    compileOnly("eu.cloudnetservice.cloudnet:driver-api:4.0.0-RC14")
    compileOnly("eu.cloudnetservice.cloudnet:bridge-api:4.0.0-RC14")
    compileOnly("com.imaginarycode.minecraft:RedisBungee:0.3.6-SNAPSHOT")
    compileOnly("net.kyori:adventure-text-minimessage:4.17.0")
    compileOnly("net.kyori:adventure-platform-bungeecord:4.3.3")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.hsqldb:hsqldb:2.7.3")
    implementation("commons-io:commons-io:2.16.1")
    implementation("org.apache.commons:commons-lang3:3.14.0")
    implementation("org.bstats:bstats-velocity:3.2.1")
}

tasks.shadowJar {
    configurations = listOf(project.configurations.runtimeClasspath.get())
    relocate("org.bstats", "${project.group}.bstats")
}

tasks {
    runVelocity {
        downloadPlugins {
            url("https://download.luckperms.net/1624/velocity/LuckPerms-Velocity-5.5.36.jar")
        }
        velocityVersion("3.4.0-SNAPSHOT")
    }
}

val targetJavaVersion = 21
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}

val templateSource = file("src/main/templates")
val templateDest = layout.buildDirectory.dir("generated/sources/templates")

val generateTemplates = tasks.register<Copy>("generateTemplates") {
    val props = mapOf("version" to project.version)

    inputs.properties(props)
    from(templateSource)
    into(templateDest)
    expand(props)
}

// Add generated templates to main source set
sourceSets.main {
    java {
        // Correct usage for TaskProvider<Copy>
        srcDir(generateTemplates.map { it.destinationDir })
    }
}

// Optional: Eclipse integration
project.plugins.withId("eclipse") {
    project.eclipse.synchronizationTasks(generateTemplates)
}