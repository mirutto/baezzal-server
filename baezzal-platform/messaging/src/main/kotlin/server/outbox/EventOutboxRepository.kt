package server.outbox

import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface EventOutboxRepository : JpaRepository<EventOutbox, Long> {
    fun findAllByPublishedAtIsNullOrderByCreatedAtAsc(pageRequest: PageRequest): List<EventOutbox>

    fun findUnpublished(limit: Int): List<EventOutbox> =
        findAllByPublishedAtIsNullOrderByCreatedAtAsc(PageRequest.of(0, limit))

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
        UPDATE EventOutbox e
        SET e.publishedAt = :publishedAt
        WHERE e.id IN :ids
          AND e.publishedAt IS NULL
    """,
    )
    fun markPublishedByIds(
        ids: List<Long>,
        publishedAt: LocalDateTime,
    ): Int
}
