plugins {
    `java-library`
}

group = "club.revived"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://mvn.revived.club/releases")
}

configurations.all {
    resolutionStrategy {
        force("com.google.protobuf:protobuf-java:4.34.1")
        force("com.google.protobuf:protobuf-java-util:4.34.1")
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Test> {
    useJUnitPlatform()
}

sourceSets {
    main {
        java {
            srcDir("${rootProject.projectDir}/proto/gen/java")
        }
    }
}

// Create or reference the root-level generateProto task
val generateProtoTask = rootProject.tasks.findByName("generateProto") ?: rootProject.tasks.register<Exec>("generateProto") {
    workingDir(rootProject.projectDir.resolve("proto"))
    commandLine("buf", "generate", "proto")
}.get()

tasks.named("compileJava") {
    dependsOn(generateProtoTask)
}
