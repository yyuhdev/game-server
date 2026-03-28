plugins {
    id("revived.paper-conventions")
    id("com.gradleup.shadow")
}

dependencies {
    api(project(":game:game-api"))
    implementation(project(":shared"))
    
    implementation(project(":game:minigames:bedwars"))
    implementation(project(":game:minigames:skywars"))
    implementation(project(":game:minigames:duels"))
    
    compileOnly(libs.paper.api)
    
    implementation(libs.bundles.database)
    implementation(libs.guava)
    implementation(libs.gson)
    implementation(libs.bundles.protobuf)
    
    testImplementation(libs.bundles.testing)
}

tasks.shadowJar {
    archiveClassifier.set("all")
    archiveBaseName.set("game-server")
    
    relocate("com.google.gson", "club.revived.libs.gson")
    relocate("club.revived.celery", "club.revived.libs.celery")
    relocate("com.google.common", "club.revived.libs.guava")
    relocate("com.zaxxer.hikari", "club.revived.libs.hikari")
    relocate("org.jetbrains.exposed", "club.revived.libs.exposed")
    relocate("com.google.protobuf", "club.revived.libs.protobuf")
    
    mergeServiceFiles()
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
