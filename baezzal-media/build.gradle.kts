dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation(project(":baezzal-platform:image"))
    implementation(project(":baezzal-platform:object-storage"))
    implementation(project(":baezzal-platform:messaging"))
}
