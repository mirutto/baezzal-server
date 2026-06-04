package server.usertaginterest.domain

import global.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

@Entity
@Table(
    name = "user_tag_interest",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_user_tag_interest_user_id_tag_id",
            columnNames = ["user_id", "tag_id"],
        ),
    ],
)
class UserTagInterest(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_tag_interest_id", nullable = false)
    val id: Long = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "tag_id", nullable = false)
    val tagId: Long,

    @Column(name = "score", nullable = false)
    var score: Int,

    @Column(name = "last_interacted_at", nullable = false, columnDefinition = "DATETIME(6)")
    var lastInteractedAt: LocalDateTime,
) : BaseEntity() {
    fun increaseScore(
        delta: Int,
        interactedAt: LocalDateTime,
    ) {
        score += delta
        lastInteractedAt = interactedAt
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserTagInterest) return false
        return id != 0L && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()
}
