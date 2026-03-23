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
    implementation(project(":game:minigames:spleef"))
    
    compileOnly(libs.paper.api)
    
    implementation(libs.bundles.database)
    implementation(libs.jedis)
    implementation(libs.guava)
    implementation(libs.gson)
    implementation(libs.compositio)
    implementation(libs.concordia)
    
    testImplementation(libs.bundles.testing)
}

tasks.shadowJar {
    archiveClassifier.set("all")
    archiveBaseName.set("game-server")
    
    relocate("com.google.gson", "club.revived.libs.gson")
    relocate("com.google.common", "club.revived.libs.guava")
    relocate("com.zaxxer.hikari", "club.revived.libs.hikari")
    relocate("org.jetbrains.exposed", "club.revived.libs.exposed")
    
    mergeServiceFiles()
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
