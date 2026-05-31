package server.follow.implementation

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.follow.domain.Follow
import server.follow.infrastructure.FollowRepository

@Component
class FollowWriter(
    private val followRepository: FollowRepository,
) {
    @Transactional
    fun write(follow: Follow): Follow = followRepository.save(follow)
}
