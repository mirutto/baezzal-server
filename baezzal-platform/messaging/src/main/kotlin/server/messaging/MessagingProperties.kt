package server.messaging

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "redis.messaging")
class MessagingProperties {
    var defaultChannel: String = "baezzal-default"
}
