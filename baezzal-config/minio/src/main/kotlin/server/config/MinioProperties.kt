package server.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "minio")
data class MinioProperties(
    var uploadEndpoint: String = "http://localhost:9000",
    var publicEndpoint: String = uploadEndpoint,
    var accessKey: String = "",
    var secretKey: String = "",
)
