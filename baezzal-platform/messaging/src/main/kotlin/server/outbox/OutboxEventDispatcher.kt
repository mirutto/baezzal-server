package server.outbox

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import server.messaging.EventPublisher

@Component
class OutboxEventDispatcher(
    private val eventPublisher: EventPublisher,
    private val eventOutboxRepository: EventOutboxRepository,
    private val outboxEventMarker: OutboxEventMarker,
) {
    private val logger = KotlinLogging.logger {}

    fun dispatchBatch(batchSize: Int): Boolean {
        val rows = eventOutboxRepository.findUnpublished(batchSize)
        if (rows.isEmpty()) return true

        val publishedIds = rows.mapNotNull(::publishEvent)
        outboxEventMarker.markPublished(publishedIds)
        return true
    }

    private fun publishEvent(row: EventOutbox): Long? =
        runCatching {
            eventPublisher.publish(
                type = row.type,
                payloadJson = row.payload,
                eventId = row.eventId,
            )
            row.id
        }.onFailure { e ->
            logger.warn(e) {
                "Outbox publish failed. outboxId=${row.id} topic=${row.topic}"
            }
        }.getOrNull()
}
