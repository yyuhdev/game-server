plugins {
    id("revived.common-conventions")
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://mvn.revived.club/releases")
}



tasks.processResources {
    val props = mapOf(
        "version" to project.version,
        "name" to project.name
    )
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}
