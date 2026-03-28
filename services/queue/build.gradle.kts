plugins {
    id("revived.service-conventions")
}

dependencies {
    implementation(libs.bundles.database)
    
    implementation(libs.jedis)
    
    implementation(libs.bundles.protobuf)
    
    implementation(libs.guava)
    
    testImplementation(libs.bundles.testing)
}

tasks.shadowJar {
    archiveBaseName.set("queue-service")
    
    relocate("com.google.gson", "club.revived.libs.gson")
    relocate("club.revived.celery", "club.revived.libs.celery")
    relocate("com.google.common", "club.revived.libs.guava")
    relocate("com.zaxxer.hikari", "club.revived.libs.hikari")
    relocate("redis.clients.jedis", "club.revived.libs.jedis")
    
    mergeServiceFiles()
}
