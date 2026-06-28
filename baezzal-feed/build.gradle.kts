apply(plugin = "org.jetbrains.kotlin.plugin.jpa")

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.linecorp.kotlin-jdsl:jpql-dsl:3.8.0")
    implementation("com.linecorp.kotlin-jdsl:jpql-render:3.8.0")
    implementation("com.linecorp.kotlin-jdsl:spring-data-jpa-support:3.8.0")

    implementation(project(":baezzal-config:jpa"))
    implementation(project(":baezzal-config:mysql"))
    implementation(project(":baezzal-config:redis"))
    implementation(project(":baezzal-config:swagger"))
    implementation(project(":baezzal-platform:web"))
    implementation(project(":baezzal-platform:messaging"))
}
