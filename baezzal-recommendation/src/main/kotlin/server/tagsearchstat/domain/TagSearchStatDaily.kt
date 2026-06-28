package server.tagsearchstat.domain

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
    name = "tag_search_stat_daily",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_tag_search_stat_daily_tag_id_stat_date",
            columnNames = ["tag_id", "stat_date"],
        ),
    ],
)
class TagSearchStatDaily(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_search_stat_daily_id", nullable = false)
    val id: Long = 0,

    @Column(name = "tag_id", nullable = false)
    val tagId: Long,

    @Column(name = "stat_date", nullable = false)
    val statDate: LocalDate,

    @Column(name = "search_count", nullable = false)
    var searchCount: Long = 0,

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
    fun increaseSearchCount(delta: Long = 1) {
        searchCount += delta
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TagSearchStatDaily) return false
        return id != 0L && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()
}
