package server.userinterest.implementation

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.userinterest.domain.UserInterest
import server.userinterest.infrastructure.UserInterestRepository

@Component
class UserInterestReader(
    private val userInterestRepository: UserInterestRepository,
) {
    @Transactional(readOnly = true)
    fun readAllByUserIdAndTagIds(
        userId: Long,
        tagIds: Collection<Long>,
    ): List<UserInterest> {
        if (tagIds.isEmpty()) {
            return emptyList()
        }

        return userInterestRepository.findAllByUserIdAndTagIdIn(userId, tagIds)
    }
}
