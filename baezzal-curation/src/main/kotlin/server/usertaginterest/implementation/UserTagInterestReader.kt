package server.usertaginterest.implementation

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.usertaginterest.domain.UserTagInterest
import server.usertaginterest.infrastructure.UserTagInterestRepository

@Component
class UserTagInterestReader(
    private val userTagInterestRepository: UserTagInterestRepository,
) {
    @Transactional(readOnly = true)
    fun readAllByUserIdAndTagIds(
        userId: Long,
        tagIds: Collection<Long>,
    ): List<UserTagInterest> {
        if (tagIds.isEmpty()) {
            return emptyList()
        }

        return userTagInterestRepository.findAllByUserIdAndTagIdIn(userId, tagIds)
    }
}
