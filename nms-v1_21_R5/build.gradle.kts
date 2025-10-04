plugins {
    id("java-library")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
}

dependencies {
    api(project(":api"))
    compileOnly(project(":plugin"))
    compileOnly("me.clip:placeholderapi:2.11.6")

    paperweight.paperDevBundle("1.21.5-R0.1-SNAPSHOT")
}