package server.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.connection.stream.StreamRecords
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class EventPublisher(
    private val redis: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
    private val messagingProperties: MessagingProperties,
) {
    fun publish(
        event: Any,
        eventId: String = UUID.randomUUID().toString(),
    ) {
        publish(
            type =
                requireNotNull(event::class.simpleName) {
                    "event type must not be null"
                },
            payloadJson = objectMapper.writeValueAsString(event),
            eventId = eventId,
        )
    }

    fun publish(
        type: String,
        payloadJson: String,
        eventId: String = UUID.randomUUID().toString(),
    ) {
        require(type.isNotBlank()) { "type must not be blank" }
        require(eventId.isNotBlank()) { "eventId must not be blank" }

        val record =
            StreamRecords
                .mapBacked<String, String, String>(
                    mapOf(
                        "type" to type,
                        "eventId" to eventId,
                        "occurredAt" to Instant.now().toString(),
                        "payload" to payloadJson,
                    ),
                ).withStreamKey(messagingProperties.defaultChannel)

        redis.opsForStream<String, String>().add(record)
            ?: throw IllegalStateException("Redis XADD returned null")
    }
}
