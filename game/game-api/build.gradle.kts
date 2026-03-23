plugins {
    id("revived.paper-conventions")
}

dependencies {
    api(project(":shared"))
    compileOnly(libs.paper.api)
}
