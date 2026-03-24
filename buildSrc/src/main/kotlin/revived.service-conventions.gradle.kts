plugins {
    `java-library`
    id("com.gradleup.shadow")
}

group = "club.revived"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
    maven("https://mvn.revived.club/releases")
}

dependencies {
    "implementation"(project(":shared"))
    "implementation"("com.google.code.gson:gson:2.12.1")
    "testImplementation"("org.junit.jupiter:junit-jupiter:5.11.4")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveClassifier.set("all")
    mergeServiceFiles()
    
    manifest {
        attributes["Main-Class"] = "${project.group}.services.${project.name}.Main"
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
