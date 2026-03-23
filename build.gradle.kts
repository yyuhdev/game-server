plugins {
    base
}

group = "club.revived"
version = "1.0.0-SNAPSHOT"

subprojects {
    group = rootProject.group
    version = rootProject.version
}

val outputDir = layout.projectDirectory.dir("output")

tasks.register<Sync>("collectPlugins") {
    group = "distribution"
    description = "Collects game-server JAR into output/plugins"
    
    into(outputDir.dir("plugins"))
    
    val gameServerShadow = project(":game:game-server").tasks.named("shadowJar")
    from(gameServerShadow) { rename { "game-server.jar" } }
}

tasks.register<Sync>("collectServices") {
    group = "distribution"
    description = "Collects all microservice JARs into output/services"
    
    into(outputDir.dir("services"))
    
    subprojects.filter { it.path.startsWith(":services:") && !it.name.startsWith(".") }.forEach { serviceProject ->
        val shadowTask = serviceProject.tasks.named("shadowJar")
        from(shadowTask) { rename { "${serviceProject.name}.jar" } }
    }
}

tasks.register("collectAll") {
    group = "distribution"
    description = "Collects all artifacts into output/"
    
    dependsOn("collectPlugins", "collectServices")
}

tasks.build {
    dependsOn("collectAll")
}
