package server.usertaginterest.infrastructure

import org.springframework.data.jpa.repository.JpaRepository
import server.usertaginterest.domain.UserTagInterest

interface UserTagInterestRepository : JpaRepository<UserTagInterest, Long> {
    fun findAllByUserIdAndTagIdIn(
        userId: Long,
        tagIds: Collection<Long>,
    ): List<UserTagInterest>
}
