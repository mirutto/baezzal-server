package server.auth.infrastructure

import org.springframework.stereotype.Component
import server.cache.RedisCache

@Component
class RefreshTokenCache(
    private val redisCache: RedisCache,
) {
    fun get(memberId: Long): String? =
        redisCache.get(
            key = key(memberId),
            type = String::class.java,
        )

    fun set(
        memberId: Long,
        refreshToken: String,
        ttlMillis: Long,
    ) {
        redisCache.set(
            key = key(memberId),
            value = refreshToken,
            ttlMillis = ttlMillis,
        )
    }

    private fun key(memberId: Long): String = "auth:refresh:$memberId"
}
