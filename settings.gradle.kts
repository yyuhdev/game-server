rootProject.name = "revived-game-server"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://mvn.revived.club/releases")
    }
}

include("shared")

include("game:game-api")
include("game:game-server")

include("game:minigames:bedwars")
include("game:minigames:skywars")
include("game:minigames:duels")
include("game:minigames:spleef")
