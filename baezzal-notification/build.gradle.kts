apply(plugin = "org.jetbrains.kotlin.plugin.jpa")

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation(project(":baezzal-config:jpa"))
    implementation(project(":baezzal-config:mysql"))
    implementation(project(":baezzal-config:jwt"))
    implementation(project(":baezzal-config:swagger"))
    implementation(project(":baezzal-platform:messaging"))
    implementation(project(":baezzal-platform:token"))
    implementation(project(":baezzal-platform:web"))
}
