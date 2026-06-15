package server.follow.domain

import global.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "follow",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_follow_follower_id_followee_id",
            columnNames = ["follower_id", "followee_id"],
        ),
    ],
)
class Follow(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "follow_id", nullable = false)
    val id: Long = 0,

    @Column(name = "follower_id", nullable = false)
    val followerId: Long,

    @Column(name = "followee_id", nullable = false)
    val followeeId: Long,
) : BaseEntity() {
    init {
        require(followerId != followeeId) { "자기 자신은 팔로우할 수 없습니다" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Follow) return false
        return id != 0L && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()
}
