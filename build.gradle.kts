import java.util.Date

plugins {
    java
    idea
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.openrewrite.rewrite") version "7.7.0"
}

group = "com.genesis"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf(
        "-Xlint:-serial",
        "-Xlint:-unchecked", 
        "-Xlint:-deprecation",
        "-Xlint:-static",
        "-Xlint:-removal"
    ))
}

dependencies {
    // Logging
    implementation("org.apache.logging.log4j:log4j-api:2.23.1")
    implementation("org.apache.logging.log4j:log4j-core:2.23.1")
    
    // Database
    implementation("com.zaxxer:HikariCP:7.0.2")
    
    // Utilities
    implementation("commons-io:commons-io:2.15.1")
    implementation("org.apache.commons:commons-lang3:3.14.0")
    implementation("org.apache.commons:commons-math3:3.6.1")
    
    // Custom libraries (local JARs)
    implementation(files("lib/commons-8.0.jar"))
    implementation(files("lib/emu-dev.jar"))
    implementation(files("lib/napile-1.0.5b.jar"))
    implementation(files("lib/l2e-fakes.jar"))
    
    // OpenRewrite for Java migration
    rewrite("org.openrewrite.recipe:rewrite-migrate-java:3.4.0")
    rewrite("org.openrewrite:rewrite-java:9.4.0")
    
    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.mockito:mockito-core:5.10.0")
    testImplementation("org.assertj:assertj-core:3.25.3")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes(
            "Built-By" to System.getProperty("user.name"),
            "Built-Date" to Date().toString(),
            "Implementation-URL" to "https://github.com/irsjofka-alt/Genesis-Default"
        )
    }
}

tasks.register<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowGameserverJar") {
    archiveBaseName.set("genesis-gameserver")
    archiveClassifier.set("")
    from(sourceSets.main.get().output)
    configurations = listOf(project.configurations.runtimeClasspath.get())
    manifest {
        attributes("Main-Class" to "gameserver.GameServer")
    }
    exclude("loginserver/**")
}

tasks.register<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowLoginserverJar") {
    archiveBaseName.set("genesis-loginserver")
    archiveClassifier.set("")
    from(sourceSets.main.get().output)
    configurations = listOf(project.configurations.runtimeClasspath.get())
    manifest {
        attributes("Main-Class" to "loginserver.LoginServer")
    }
    exclude("gameserver/**")
    exclude("fake/**")
}

tasks.register<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowFakeplayersJar") {
    archiveBaseName.set("genesis-fakeplayers")
    archiveClassifier.set("")
    from(sourceSets.main.get().output)
    configurations = listOf(project.configurations.runtimeClasspath.get())
    exclude("gameserver/**")
    exclude("loginserver/**")
}

tasks.register("buildAll") {
    dependsOn("shadowGameserverJar", "shadowLoginserverJar", "shadowFakeplayersJar")
    group = "build"
    description = "Build all JARs: gameserver, loginserver, and fakeplayers"
}

tasks.register("cleanAll") {
    dependsOn("clean")
    group = "build"
    description = "Clean all build outputs"
}