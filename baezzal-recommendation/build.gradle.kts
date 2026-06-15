apply(plugin = "org.jetbrains.kotlin.plugin.jpa")

dependencies {
    implementation(project(":baezzal-config:jpa"))
    implementation(project(":baezzal-config:mysql"))
    implementation(project(":baezzal-config:redis"))
    implementation(project(":baezzal-platform:messaging"))
}
