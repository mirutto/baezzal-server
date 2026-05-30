package server.outbox

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import server.messaging.MessagingProperties
import java.util.UUID

@Component
class TransactionalEventPublisher(
    private val eventOutboxRepository: EventOutboxRepository,
    private val objectMapper: ObjectMapper,
    private val messagingProperties: MessagingProperties,
) {
    @Transactional(propagation = Propagation.MANDATORY)
    fun publish(event: Any) {
        val type =
            requireNotNull(
                event::class.simpleName,
            ) { "event type must not be null" }
        val outbox =
            EventOutbox(
                topic = messagingProperties.defaultChannel,
                type = type,
                eventId = UUID.randomUUID().toString(),
                payload = objectMapper.writeValueAsString(event),
            )
        eventOutboxRepository.save(outbox)
    }
}
