package server.post.infrastructure

import org.springframework.stereotype.Component
import server.cache.CacheMemory
import server.set.SetMemory

@Component
class PostViewCountBatchCache(
    private val cacheMemory: CacheMemory,
    private val setMemory: SetMemory,
) {
    fun readPostIds(): Set<Long> =
        setMemory.members(VIEWED_POST_IDS_KEY)
            .mapNotNull(String::toLongOrNull)
            .toSet()

    fun readViewCount(postId: Long): Long =
        cacheMemory.get(
            key = viewCountKey(postId),
            type = Long::class.java,
        ) ?: 0L

    fun decreaseViewCount(
        postId: Long,
        viewCount: Long,
    ): Long = cacheMemory.decrBy(viewCountKey(postId), viewCount)

    private fun viewCountKey(postId: Long): String = "feed:post:view-count:$postId"

    companion object {
        private const val VIEWED_POST_IDS_KEY = "feed:post:viewed-post-ids"
    }
}
