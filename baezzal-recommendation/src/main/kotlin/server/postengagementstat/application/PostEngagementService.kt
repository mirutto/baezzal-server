package server.postengagementstat.application

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import server.postengagementstat.implementation.PostEngagementStatDailyWriter

@Service
class PostEngagementService(
    private val postEngagementStatDailyWriter: PostEngagementStatDailyWriter,
) {
    @Transactional
    fun recordPostView(event: PostViewedEvent) {
        postEngagementStatDailyWriter.incrementViewCount(
            postId = event.postId,
            statDate = event.viewedAt.toLocalDate(),
        )
    }

    @Transactional
    fun recordCollectionPostAdded(event: CollectionPostAddedEvent) {
        postEngagementStatDailyWriter.incrementCollectionAddedCount(
            postId = event.postId,
            statDate = event.addedAt.toLocalDate(),
        )
    }
}
