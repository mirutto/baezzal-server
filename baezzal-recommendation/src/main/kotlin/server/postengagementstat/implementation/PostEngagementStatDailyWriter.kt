package server.postengagementstat.implementation

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.postengagementstat.infrastructure.PostEngagementStatDailyRepository
import java.time.LocalDate

@Component
class PostEngagementStatDailyWriter(
    private val postEngagementStatDailyRepository: PostEngagementStatDailyRepository,
) {
    @Transactional
    fun incrementViewCount(
        postId: Long,
        statDate: LocalDate,
    ) {
        postEngagementStatDailyRepository.incrementViewCount(
            postId = postId,
            statDate = statDate,
        )
    }

    @Transactional
    fun incrementCollectionAddedCount(
        postId: Long,
        statDate: LocalDate,
    ) {
        postEngagementStatDailyRepository.incrementCollectionAddedCount(
            postId = postId,
            statDate = statDate,
        )
    }
}
