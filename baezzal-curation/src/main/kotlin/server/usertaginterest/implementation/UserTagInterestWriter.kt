package server.usertaginterest.implementation

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.usertaginterest.domain.UserTagInterest
import server.usertaginterest.infrastructure.UserTagInterestRepository

@Component
class UserTagInterestWriter(
    private val userTagInterestRepository: UserTagInterestRepository,
) {
    @Transactional
    fun writeAll(userTagInterests: Collection<UserTagInterest>): List<UserTagInterest> {
        if (userTagInterests.isEmpty()) {
            return emptyList()
        }

        return userTagInterestRepository.saveAll(userTagInterests)
    }
}
