package server.post.implementation

import org.springframework.stereotype.Component
import server.post.infrastructure.PostViewCountBatchCache

@Component
class PostViewCountBatchReader(
    private val postViewCountBatchCache: PostViewCountBatchCache,
) {
    fun readPendingViewCounts(): Map<Long, Long> =
        postViewCountBatchCache.readPostIds()
            .associateWith(postViewCountBatchCache::readViewCount)
            .filterValues { it > 0L }

    fun decreaseViewCount(
        postId: Long,
        viewCount: Long,
    ): Long = postViewCountBatchCache.decreaseViewCount(postId, viewCount)
}
