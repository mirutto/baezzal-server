apply(plugin = "org.jetbrains.kotlin.plugin.jpa")

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")

    implementation(project(":baezzal-config:jpa"))
    implementation(project(":baezzal-config:mysql"))
    implementation(project(":baezzal-config:redis"))
    implementation(project(":baezzal-config:oauth"))
    implementation(project(":baezzal-platform:cache"))
    implementation(project(":baezzal-platform:image"))
    implementation(project(":baezzal-platform:lock"))
    implementation(project(":baezzal-platform:object-storage"))
    implementation(project(":baezzal-platform:queue"))
    implementation(project(":baezzal-platform:set"))
    implementation(project(":baezzal-platform:messaging"))
    implementation(project(":baezzal-platform:token"))
}
