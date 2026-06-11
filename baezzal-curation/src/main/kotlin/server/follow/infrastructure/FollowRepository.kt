package server.follow.infrastructure

import org.springframework.data.jpa.repository.JpaRepository
import server.follow.domain.Follow

interface FollowRepository : JpaRepository<Follow, Long> {
    fun existsByFollowerIdAndFolloweeId(
        followerId: Long,
        followeeId: Long,
    ): Boolean

    fun findByFollowerIdAndFolloweeId(
        followerId: Long,
        followeeId: Long,
    ): Follow?

    fun countByFollowerId(followerId: Long): Long

    fun countByFolloweeId(followeeId: Long): Long
}
