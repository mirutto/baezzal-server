apply(plugin = "org.jetbrains.kotlin.plugin.jpa")

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework:spring-tx")

    implementation(project(":baezzal-config:jpa"))
    implementation(project(":baezzal-config:mysql"))
    implementation(project(":baezzal-config:redis"))
    implementation(project(":baezzal-platform:cache"))
    implementation(project(":baezzal-platform:queue"))
    implementation(project(":baezzal-platform:set"))
    implementation(project(":baezzal-platform:messaging"))
}
