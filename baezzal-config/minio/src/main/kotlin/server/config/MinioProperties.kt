package server.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "minio")
data class MinioProperties(
    var endpoint: String = "http://localhost:9000",
    var publicEndpoint: String = endpoint,
    var accessKey: String = "",
    var secretKey: String = "",
)
