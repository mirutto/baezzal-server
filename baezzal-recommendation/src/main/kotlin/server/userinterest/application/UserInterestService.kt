package server.userinterest.application

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import server.posttag.implementation.RecommendationPostTagReader
import server.userinterest.domain.UserInterest
import server.userinterest.implementation.UserInterestReader
import server.userinterest.implementation.UserInterestWriter

@Service
class UserInterestService(
    private val recommendationPostTagReader: RecommendationPostTagReader,
    private val userInterestReader: UserInterestReader,
    private val userInterestWriter: UserInterestWriter,
) {
    @Transactional
    fun recordPostView(event: PostViewedEvent) {
        accumulateInterest(
            userId = event.userId,
            postId = event.postId,
            deltaScore = CLICK_SCORE,
            interactedAt = event.viewedAt,
        )
    }

    @Transactional
    fun recordCollectionPostAdded(event: CollectionPostAddedEvent) {
        accumulateInterest(
            userId = event.userId,
            postId = event.postId,
            deltaScore = COLLECTION_ADD_SCORE,
            interactedAt = event.addedAt,
        )
    }

    private fun accumulateInterest(
        userId: Long,
        postId: Long,
        deltaScore: Int,
        interactedAt: java.time.LocalDateTime,
    ) {
        val tagIds = recommendationPostTagReader.readAllByPostId(postId)
            .map { it.tagId }
            .distinct()

        if (tagIds.isEmpty()) {
            return
        }

        val existingByTagId = userInterestReader.readAllByUserIdAndTagIds(userId, tagIds)
            .associateBy { it.tagId }

        val userInterests = tagIds.map { tagId ->
            existingByTagId[tagId]?.apply {
                increaseScore(
                    delta = deltaScore,
                    interactedAt = interactedAt,
                )
            } ?: UserInterest(
                userId = userId,
                tagId = tagId,
                score = deltaScore,
                lastInteractedAt = interactedAt,
            )
        }

        userInterestWriter.writeAll(userInterests)
    }

    companion object {
        private const val CLICK_SCORE = 2
        private const val COLLECTION_ADD_SCORE = 5
    }
}
