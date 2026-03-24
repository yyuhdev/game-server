plugins {
    id("revived.paper-conventions")
    id("com.gradleup.shadow")
    alias(libs.plugins.protobuf)
}

dependencies {
    api(project(":game:game-api"))
    implementation(project(":shared"))
    
    implementation(project(":game:minigames:bedwars"))
    implementation(project(":game:minigames:skywars"))
    implementation(project(":game:minigames:duels"))
    implementation(project(":game:minigames:spleef"))
    
    compileOnly(libs.paper.api)
    
    implementation(libs.bundles.database)
    implementation(libs.guava)
    implementation(libs.gson)
    implementation(libs.compositio)
    implementation(libs.concordia)
    implementation(libs.bundles.protobuf)
    
    testImplementation(libs.bundles.testing)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.29.3"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.70.0"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                create("grpc")
            }
        }
    }
}

sourceSets {
    main {
        java {
            srcDirs("build/generated/source/proto/main/grpc")
            srcDirs("build/generated/source/proto/main/java")
        }
    }
}

tasks.shadowJar {
    archiveClassifier.set("all")
    archiveBaseName.set("game-server")
    
    relocate("com.google.gson", "club.revived.libs.gson")
    relocate("com.google.common", "club.revived.libs.guava")
    relocate("com.zaxxer.hikari", "club.revived.libs.hikari")
    relocate("org.jetbrains.exposed", "club.revived.libs.exposed")
    relocate("io.grpc", "club.revived.libs.grpc")
    relocate("com.google.protobuf", "club.revived.libs.protobuf")
    
    mergeServiceFiles()
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
