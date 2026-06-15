package server.auth.infrastructure

import org.springframework.stereotype.Component
import server.cache.CacheMemory

@Component
class RefreshTokenCache(
    private val cacheMemory: CacheMemory,
) {
    fun get(sessionId: String): String? =
        cacheMemory.get(
            key = key(sessionId),
            type = String::class.java,
        )

    fun set(
        sessionId: String,
        refreshToken: String,
        ttlMillis: Long,
    ) {
        cacheMemory.set(
            key = key(sessionId),
            value = refreshToken,
            ttlMillis = ttlMillis,
        )
    }

    fun delete(sessionId: String) {
        cacheMemory.evict(key(sessionId))
    }

    private fun key(sessionId: String): String = "auth:refresh:$sessionId"
}
