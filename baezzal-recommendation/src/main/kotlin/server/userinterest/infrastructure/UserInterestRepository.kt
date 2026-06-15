package server.userinterest.infrastructure

import org.springframework.data.jpa.repository.JpaRepository
import server.userinterest.domain.UserInterest

interface UserInterestRepository : JpaRepository<UserInterest, Long> {
    fun findAllByUserIdAndTagIdIn(
        userId: Long,
        tagIds: Collection<Long>,
    ): List<UserInterest>
}
