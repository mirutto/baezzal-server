package server.postengagementstat.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(
    name = "post_engagement_stat_daily",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_post_engagement_stat_daily_post_id_stat_date",
            columnNames = ["post_id", "stat_date"],
        ),
    ],
)
class PostEngagementStatDaily(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_engagement_stat_daily_id", nullable = false)
    val id: Long = 0,

    @Column(name = "post_id", nullable = false)
    val postId: Long,

    @Column(name = "stat_date", nullable = false)
    val statDate: LocalDate,

    @Column(name = "view_count", nullable = false)
    var viewCount: Long = 0,

    @Column(name = "collection_added_count", nullable = false)
    var collectionAddedCount: Long = 0,

    @CreatedDate
    @Column(
        name = "created_at",
        nullable = false,
        updatable = false,
        columnDefinition = "DATETIME(6)",
    )
    var createdAt: LocalDateTime? = null,

    @LastModifiedDate
    @Column(
        name = "updated_at",
        nullable = false,
        columnDefinition = "DATETIME(6)",
    )
    var updatedAt: LocalDateTime? = null,
) {
    fun increaseViewCount(delta: Long = 1) {
        viewCount += delta
    }

    fun increaseCollectionAddedCount(delta: Long = 1) {
        collectionAddedCount += delta
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PostEngagementStatDaily) return false
        return id != 0L && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()
}
