tasks.getByName("bootJar") {
    enabled = true
}

tasks.getByName("jar") {
    enabled = false
}

dependencies {
    implementation(project(":baezzal-curation"))
    implementation(project(":baezzal-feed"))
    implementation(project(":baezzal-media"))
    implementation(project(":baezzal-notification"))
}
