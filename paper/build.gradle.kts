import org.gradle.api.tasks.compile.JavaCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("eclipse")
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.8"
    id("com.gradleup.shadow") version "9.3.1"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

group = "net.hnt8"

val targetJavaVersion = 21

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    maven("https://repo.codemc.io/repository/creatorfromhell/")
    maven("https://repo.md-5.net/content/repositories/snapshots/")
    maven("https://repo.md-5.net/content/repositories/releases/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation(project(":core"))

    // Kyori Adventure
    implementation("net.kyori:adventure-api:4.26.1")
    implementation("net.kyori:adventure-text-minimessage:4.26.1")
    implementation("net.kyori:adventure-text-serializer-gson:4.14.0")

    // Utilities
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.hsqldb:hsqldb:2.7.3")
    implementation("commons-io:commons-io:2.16.1")
    implementation("org.apache.commons:commons-lang3:3.14.0")
    implementation("org.bstats:bstats-bukkit:3.2.1")
    implementation("com.googlecode.json-simple:json-simple:1.1.1")

    // Paper / Plugins
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("net.milkbowl.vault:VaultUnlockedAPI:2.16")
    compileOnly("net.luckperms:api:5.2")
    compileOnly("eu.cloudnetservice.cloudnet:driver-api:4.0.0-RC14")
    compileOnly("eu.cloudnetservice.cloudnet:bridge-api:4.0.0-RC14")
    compileOnly("com.imaginarycode.minecraft:RedisBungee:0.3.6-SNAPSHOT")

    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}

tasks.named<ShadowJar>("shadowJar") {
    configurations = listOf(project.configurations.runtimeClasspath.get())
    relocate("org.bstats", project.group.toString())
}

tasks.named<ProcessResources>("processResources") {
    val props = mapOf("version" to rootProject.version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}

tasks.runServer {
    minecraftVersion("1.21.11")
    jvmArgs("-Xms512M", "-Xmx2G")
}

runPaper.folia.registerTask()