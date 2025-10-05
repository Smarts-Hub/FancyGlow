plugins {
    id("java")
    id("io.github.goooler.shadow") version "8.1.7"
}

dependencies {
    implementation(project(":api"))
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")

    implementation("io.github.revxrsal:lamp.common:4.0.0-rc.12")
    implementation("io.github.revxrsal:lamp.bukkit:4.0.0-rc.12")

    implementation("dev.dejvokep:boosted-yaml:1.3.6")
    implementation("com.h2database:h2:2.1.214")

    compileOnly("net.kyori:adventure-api:4.17.0")
    compileOnly("net.kyori:adventure-platform-bukkit:4.4.1")
    compileOnly("net.kyori:adventure-text-minimessage:4.17.0")

    compileOnly("me.clip:placeholderapi:2.11.6")
}

tasks {
    shadowJar {
        archiveFileName.set("FancyGlow-${project.version}.jar")
        archiveClassifier.set("")

        dependsOn(
            ":api:jar",
            ":nms-v1_21_R5:jar"
        )

        from(zipTree(project(":api").tasks.named<Jar>("jar").get().archiveFile))
        from(zipTree(project(":nms-v1_21_R5").tasks.named<Jar>("jar").get().archiveFile))

        relocate("org.h2", "dev.smartshub.fancyglow.libs.h2")
        relocate("revxrsal.commands", "dev.smartshub.fancyglow.libs.lamp")
        relocate("dev.dejvokep.boostedyaml", "dev.smartshub.fancyglow.libs.boostedyaml")

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        exclude("**/org/jetbrains/**")
        exclude("**/org/intellij/**")
        exclude("META-INF/MANIFEST.MF")
        exclude("**/*.kotlin_metadata")
        exclude("**/*.kotlin_module")
        exclude("**/*.SF")
        exclude("**/*.DSA")
        exclude("**/*.RSA")
    }

    processResources {
        filesMatching("plugin.yml") {
            expand("version" to project.version)
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        }
    }
    build {
        dependsOn(shadowJar)
    }
}