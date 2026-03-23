plugins {
    id("revived.paper-conventions")
}

dependencies {
    "implementation"(project(":game:game-api"))
    "implementation"(project(":shared"))
    "compileOnly"("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
}
