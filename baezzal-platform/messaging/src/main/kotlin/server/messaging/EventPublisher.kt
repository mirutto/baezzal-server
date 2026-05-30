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
            type = eventTypeOf(event),
            payloadJson = objectMapper.writeValueAsString(event),
            eventId = eventId,
        )
    }

    fun publish(
        type: String,
        payloadJson: String,
        eventId: String = UUID.randomUUID().toString(),
    ) {
        validate(type, eventId)

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
            ?: throw MessagingException("Redis XADD returned null")
    }

    private fun eventTypeOf(event: Any): String =
        event::class.simpleName ?: throw MessagingException("event type must not be null")

    private fun validate(
        type: String,
        eventId: String,
    ) {
        if (type.isBlank()) {
            throw MessagingException("type must not be blank")
        }
        if (eventId.isBlank()) {
            throw MessagingException("eventId must not be blank")
        }
    }
}
