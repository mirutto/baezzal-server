tasks.getByName("bootJar") {
    enabled = true
}

tasks.getByName("jar") {
    enabled = false
}

dependencies {
    implementation(project(":baezzal-community"))
    implementation(project(":baezzal-feed"))
    implementation(project(":baezzal-recommendation"))
    implementation(project(":baezzal-media"))
    implementation(project(":baezzal-notification"))
}
