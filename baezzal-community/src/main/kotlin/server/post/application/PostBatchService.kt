package server.post.application

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import server.post.implementation.PostBatchLocker
import server.post.implementation.PostViewCountBatchReader
import server.post.implementation.PostWriter

@Service
class PostBatchService(
    private val postBatchLocker: PostBatchLocker,
    private val postWriter: PostWriter,
    private val postViewCountBatchReader: PostViewCountBatchReader,
) {
    @Transactional
    fun updateViewCounts(): PostBatchResult =
        postBatchLocker.withLock {
            val viewCountByPostId = postViewCountBatchReader.readPendingViewCounts()

            if (viewCountByPostId.isEmpty()) {
                return@withLock PostBatchResult(
                    postCount = 0,
                    viewCount = 0L,
                )
            }

            postWriter.increaseViewCounts(viewCountByPostId)

            viewCountByPostId.forEach { (postId, viewCount) ->
                postViewCountBatchReader.decreaseViewCount(
                    postId = postId,
                    viewCount = viewCount,
                )
            }

            PostBatchResult(
                postCount = viewCountByPostId.size,
                viewCount = viewCountByPostId.values.sum(),
            )
        }
}
