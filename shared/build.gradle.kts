plugins {
    id("revived.common-conventions")
}

dependencies {
    compileOnly(libs.paper.api)
    
    api(libs.gson)
    api(libs.guava)
    api(libs.bundles.adventure)
    api(libs.configurate.yaml)
    api(libs.bundles.protobuf)
    
    testImplementation(libs.bundles.testing)
}
