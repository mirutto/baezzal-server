dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework:spring-tx")

    implementation(project(":baezzal-platform:image"))
    implementation(project(":baezzal-platform:object-storage"))
}
