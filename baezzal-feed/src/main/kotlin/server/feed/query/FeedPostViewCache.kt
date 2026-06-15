package server.feed.query

import org.springframework.stereotype.Component
import server.cache.CacheMemory
import server.set.SetMemory

@Component
class FeedPostViewCache(
    private val cacheMemory: CacheMemory,
    private val setMemory: SetMemory,
) {
    fun recordView(postId: Long): Long {
        val viewCount = cacheMemory.incr(viewCountKey(postId))
        setMemory.add(VIEWED_POST_IDS_KEY, postId.toString())
        return viewCount
    }

    private fun viewCountKey(postId: Long): String = "feed:post:view-count:$postId"

    companion object {
        private const val VIEWED_POST_IDS_KEY = "feed:post:viewed-post-ids"
    }
}
