package server.auth.infrastructure

import org.springframework.stereotype.Component
import server.cache.CacheMemory

@Component
class RefreshTokenCache(
    private val cacheMemory: CacheMemory,
) {
    fun get(memberId: Long): String? =
        cacheMemory.get(
            key = key(memberId),
            type = String::class.java,
        )

    fun set(
        memberId: Long,
        refreshToken: String,
        ttlMillis: Long,
    ) {
        cacheMemory.set(
            key = key(memberId),
            value = refreshToken,
            ttlMillis = ttlMillis,
        )
    }

    fun delete(memberId: Long) {
        cacheMemory.evict(key(memberId))
    }

    private fun key(memberId: Long): String = "auth:refresh:$memberId"
}
