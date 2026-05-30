package server.outbox

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import server.messaging.MessagingException
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(
    name = "event_outbox",
    indexes = [
        Index(
            name = "idx_outbox_published_at_created_at",
            columnList = "published_at,created_at",
        ),
    ],
)
class EventOutbox(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long = 0,
    @Column(name = "topic", length = 255, nullable = false)
    val topic: String,
    @Column(name = "type", length = 255, nullable = false)
    val type: String,
    @Column(name = "event_id", length = 64, nullable = false)
    val eventId: String,
    @Column(name = "payload", columnDefinition = "json", nullable = false)
    val payload: String,
    publishedAt: LocalDateTime? = null,
) {
    @CreatedDate
    @Column(
        name = "created_at",
        nullable = false,
        updatable = false,
        columnDefinition = "DATETIME(6)",
    )
    var createdAt: LocalDateTime? = null
        protected set

    @Column(
        name = "published_at",
        columnDefinition = "DATETIME(6)",
    )
    var publishedAt: LocalDateTime? = publishedAt
        protected set

    val published: Boolean
        get() = publishedAt != null

    fun markPublished(at: LocalDateTime = LocalDateTime.now()) {
        if (published) {
            throw MessagingException("EventOutbox(id=$id) is already published")
        }
        publishedAt = at
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EventOutbox) return false
        return id != 0L && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()
}
