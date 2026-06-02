package server.notification.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(
    name = "notification_device",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_notification_device_token",
            columnNames = ["token"],
        ),
    ],
)
class NotificationDevice(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long = 0,

    @Column(name = "user_id")
    var userId: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 20)
    var platform: NotificationPlatform,

    @Column(name = "token", nullable = false, length = 511)
    val token: String,

    @Column(name = "enabled", nullable = false)
    var enabled: Boolean = true,

    @Column(name = "last_seen_at", nullable = false, columnDefinition = "DATETIME(6)")
    var lastSeenAt: LocalDateTime,
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

    @LastModifiedDate
    @Column(
        name = "updated_at",
        nullable = false,
        columnDefinition = "DATETIME(6)",
    )
    var updatedAt: LocalDateTime? = null
        protected set

    fun refresh(
        userId: Long?,
        platform: NotificationPlatform,
        lastSeenAt: LocalDateTime,
    ) {
        this.userId = userId
        this.platform = platform
        this.enabled = true
        this.lastSeenAt = lastSeenAt
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NotificationDevice) return false
        return id != 0L && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()
}
