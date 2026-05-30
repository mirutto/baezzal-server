package server.outbox

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class OutboxEventMarker(
    private val eventOutboxRepository: EventOutboxRepository,
) {
    @Transactional
    fun markPublished(outboxIds: List<Long>) {
        if (outboxIds.isEmpty()) return
        eventOutboxRepository.markPublishedByIds(
            ids = outboxIds.distinct(),
            publishedAt = LocalDateTime.now(),
        )
    }
}
