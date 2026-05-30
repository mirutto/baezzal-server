package server.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "redis.connection")
data class RedisConnectionProperties(
    var host: String = "localhost",
    var port: Int = 6379,
    var database: Int = 0,
    var username: String? = null,
    var password: String? = null,
    var timeout: Duration = Duration.ofSeconds(30),
)
