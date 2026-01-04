import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("java")
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io/")
    maven("https://libraries.minecraft.net/")
    maven("https://repo.panda-lang.org/releases/")
    maven("https://oss.sonatype.org/content/groups/public")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi")
}

dependencies {
    compileOnly(libs.com.mojang.authlib)
    compileOnly(libs.me.clip.placeholderapi)
    compileOnly(libs.org.spigotmc.spigot.api)
    compileOnly(libs.com.github.neznamy.tab.api)
    compileOnly(libs.com.github.retrooper.packetevents.spigot)

    // Change "zap" to "implementation" for all these:
    implementation(libs.net.kyori.adventure.api)
    implementation(libs.org.bstats.bstats.bukkit)
    implementation(libs.dev.rollczi.litecommands)
    implementation(libs.dev.dejvokep.boosted.yaml)
    implementation(libs.dev.rollczi.litecommands.adventure)
    implementation(libs.net.kyori.adventure.platform.bukkit)
    implementation(libs.net.kyori.adventure.text.minimessage)
}

group = "hhitt.fancyglow"
description = "FancyGlow"
version = "2.10.3"

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.imhhitt"
            artifactId = "FancyGlow"

            from(components["java"])
        }
    }
}

tasks {

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    compileJava {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    shadowJar {
        archiveFileName.set("FancyGlow-${project.version}.jar")
        destinationDirectory.set(file("$rootDir/jars/"))

        // MOVE your relocations here to prevent conflicts with other plugins
        val prefix = "${project.group}.deps"
        relocate("net.kyori", "$prefix.kyori")
        relocate("org.bstats", "$prefix.bstats")
        relocate("dev.rollczi", "$prefix.litecommands")
        relocate("dev.dejvokep.boostedyaml", "$prefix.boostedyaml")
    }

    bukkit {
        name = "FancyGlow"
        description = "Plugin that allow the player to change its glowing color."
        prefix = name
        version = project.version.toString()
        main = project.group.toString() + ".FancyGlow"
        apiVersion = "1.19"
        authors = listOf("hhitt")
        contributors = listOf("Sliide_")
        load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD
        defaultPermission = BukkitPluginDescription.Permission.Default.FALSE
        softDepend = listOf(
            "TAB",
            "PlaceholderAPI",
        )

        permissions {
            // Admin permission
            register("fancyglow.admin") {
                default = BukkitPluginDescription.Permission.Default.OP
                description = "Gives permission to every FancyGlow command and feature."
                children = listOf(
                    "fancyglow.rainbow", "fancyglow.flashing", "fancyglow.all_colors", "fancyglow.command.gui",
                    "fancyglow.command.color", "fancyglow.command.reload", "fancyglow.command.disable",
                    "fancyglow.command.disable.everyone", "fancyglow.command.disable.others", "fancyglow.command.set"
                )
            }
            // Commands related permissions
            register("fancyglow.command.gui") {
                default = BukkitPluginDescription.Permission.Default.FALSE
                description = "Gives access to color selector inventory."
            }
            register("fancyglow.command.set") {
                default = BukkitPluginDescription.Permission.Default.FALSE
                description = "Gives access to set another player glowing color."
            }
            register("fancyglow.command.disable") {
                default = BukkitPluginDescription.Permission.Default.TRUE
                description = "Gives access to disable player own color."
            }
            register("fancyglow.command.color") {
                default = BukkitPluginDescription.Permission.Default.TRUE
                description = "Gives access to select player own glow."
            }
            register("fancyglow.command.disable.others") {
                default = BukkitPluginDescription.Permission.Default.OP
                description = "Gives access to disable other players glow."
            }
            register("fancyglow.command.disable.everyone") {
                default = BukkitPluginDescription.Permission.Default.OP
                description = "Gives access to disable the whole server glow."
            }
            register("fancyglow.command.reload") {
                default = BukkitPluginDescription.Permission.Default.OP
                description = "Gives access to reload plugin config."
            }
            // Color related permissions
            register("fancyglow.all_colors") {
                default = BukkitPluginDescription.Permission.Default.FALSE
                description = "Gives permission to every color."
                children = listOf(
                    "fancyglow.black", "fancyglow.white",
                    "fancyglow.red", "fancyglow.dark_red",
                    "fancyglow.gray", "fancyglow.dark_gray",
                    "fancyglow.blue", "fancyglow.dark_blue",
                    "fancyglow.aqua", "fancyglow.dark_aqua",
                    "fancyglow.green", "fancyglow.dark_green",
                    "fancyglow.yellow", "fancyglow.gold",
                    "fancyglow.light_purple", "fancyglow.dark_purple"
                )
            }
            register("fancyglow.rainbow") {
                default = BukkitPluginDescription.Permission.Default.FALSE
                description = "Gives access to rainbow mode."
            }
            register("fancyglow.flashing") {
                default = BukkitPluginDescription.Permission.Default.FALSE
                description = "Gives access to flashing mode."
            }
            // Color permissions
            register("fancyglow.red") {
                description = "Gives access to color red."
            }
            register("fancyglow.gold") {
                description = "Gives access to color gold."
            }
            register("fancyglow.gray") {
                description = "Gives access to color gray."
            }
            register("fancyglow.blue") {
                description = "Gives access to color blue."
            }
            register("fancyglow.aqua") {
                description = "Gives access to color aqua."
            }
            register("fancyglow.black") {
                description = "Gives access to color black."
            }
            register("fancyglow.white") {
                description = "Gives access to color white."
            }
            register("fancyglow.green") {
                description = "Gives access to color green."
            }
            register("fancyglow.yellow") {
                description = "Gives access to color yellow."
            }
            register("fancyglow.dark_red") {
                description = "Gives access to color dark red."
            }
            register("fancyglow.dark_blue") {
                description = "Gives access to color dark blue."
            }
            register("fancyglow.dark_gray") {
                description = "Gives access to color dark gray."
            }
            register("fancyglow.dark_aqua") {
                description = "Gives access to color dark aqua."
            }
            register("fancyglow.dark_green") {
                description = "Gives access to color dark green."
            }
            register("fancyglow.dark_purple") {
                description = "Gives access to color dark purple o just purple."
            }
            register("fancyglow.light_purple") {
                description = "Gives access to color light_purple aka pink."
            }
        }
    }
}