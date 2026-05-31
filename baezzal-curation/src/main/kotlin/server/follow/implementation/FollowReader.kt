package server.follow.implementation

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.follow.domain.Follow
import server.follow.infrastructure.FollowRepository

@Component
class FollowReader(
    private val followRepository: FollowRepository,
) {
    @Transactional(readOnly = true)
    fun exists(
        followerId: Long,
        followeeId: Long,
    ): Boolean = followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)

    @Transactional(readOnly = true)
    fun readByFollowerIdAndFolloweeId(
        followerId: Long,
        followeeId: Long,
    ): Follow? = followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)
}
