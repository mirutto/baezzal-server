package server.usertaginterest.application

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import server.collection.application.CollectionPostAddedEvent
import server.post.application.PostViewedEvent
import server.posttag.implementation.PostTagReader
import server.usertaginterest.domain.UserTagInterest
import server.usertaginterest.implementation.UserTagInterestReader
import server.usertaginterest.implementation.UserTagInterestWriter

@Service
class UserTagInterestService(
    private val postTagReader: PostTagReader,
    private val userTagInterestReader: UserTagInterestReader,
    private val userTagInterestWriter: UserTagInterestWriter,
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
        val tagIds = postTagReader.readAllByPostId(postId)
            .map { it.tagId }
            .distinct()

        if (tagIds.isEmpty()) {
            return
        }

        val existingByTagId = userTagInterestReader.readAllByUserIdAndTagIds(userId, tagIds)
            .associateBy { it.tagId }

        val userTagInterests = tagIds.map { tagId ->
            existingByTagId[tagId]?.apply {
                increaseScore(
                    delta = deltaScore,
                    interactedAt = interactedAt,
                )
            } ?: UserTagInterest(
                userId = userId,
                tagId = tagId,
                score = deltaScore,
                lastInteractedAt = interactedAt,
            )
        }

        userTagInterestWriter.writeAll(userTagInterests)
    }

    companion object {
        private const val CLICK_SCORE = 2
        private const val COLLECTION_ADD_SCORE = 5
    }
}
