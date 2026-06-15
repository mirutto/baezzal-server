package server.userinterest.implementation

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.userinterest.domain.UserInterest
import server.userinterest.infrastructure.UserInterestRepository

@Component
class UserInterestWriter(
    private val userInterestRepository: UserInterestRepository,
) {
    @Transactional
    fun writeAll(userInterests: Collection<UserInterest>): List<UserInterest> {
        if (userInterests.isEmpty()) {
            return emptyList()
        }

        return userInterestRepository.saveAll(userInterests)
    }
}
