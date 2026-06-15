package server.follow.implementation

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.follow.domain.Follow
import server.follow.infrastructure.FollowRepository

@Component
class FollowRemover(
    private val followRepository: FollowRepository,
) {
    @Transactional
    fun remove(follow: Follow) {
        followRepository.delete(follow)
    }
}
