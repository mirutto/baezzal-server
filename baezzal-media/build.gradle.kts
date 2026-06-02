dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework:spring-tx")

    implementation(project(":baezzal-config:swagger"))
    implementation(project(":baezzal-platform:cache"))
    implementation(project(":baezzal-platform:image"))
    implementation(project(":baezzal-platform:object-storage"))
    implementation(project(":baezzal-platform:messaging"))
    implementation(project(":baezzal-platform:web"))
}
