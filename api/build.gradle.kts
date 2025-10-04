plugins {
    id("java-library")
    id("maven-publish")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
}


java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = group.toString()
            version = project.version.toString()
            artifactId = "fancy-glow-api"
            version = project.version.toString()
        }
    }
}