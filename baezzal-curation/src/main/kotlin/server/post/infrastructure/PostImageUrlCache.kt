package server.post.infrastructure

import org.springframework.stereotype.Component
import server.cache.CacheMemory

@Component
class PostImageUrlCache(
    private val cacheMemory: CacheMemory,
) {
    fun isIssued(imageUrl: String): Boolean =
        cacheMemory.get(
            key = key(imageUrl),
            type = Boolean::class.java,
        ) == true

    fun setIssued(
        imageUrl: String,
        ttlMillis: Long,
    ) {
        cacheMemory.set(
            key = key(imageUrl),
            value = true,
            ttlMillis = ttlMillis,
        )
    }

    private fun key(imageUrl: String): String = "post:image-url:$imageUrl"
}
