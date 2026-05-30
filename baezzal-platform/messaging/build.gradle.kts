apply(plugin = "org.jetbrains.kotlin.plugin.jpa")

dependencies {
    implementation(project(":baezzal-config:jpa"))
    implementation(project(":baezzal-config:redis"))
    implementation("org.springframework:spring-tx")
}
