package server.auth.infrastructure

import org.springframework.stereotype.Component
import server.auth.domain.AuthTokens
import server.cache.RedisCache
import server.cache.RedisTakeCache

@Component
class AuthTicketCache(
    private val redisCache: RedisCache,
    private val redisTakeCache: RedisTakeCache,
) {
    fun set(
        ticket: String,
        tokens: AuthTokens,
        ttlMillis: Long,
    ) {
        redisCache.set(
            key = key(ticket),
            value = tokens,
            ttlMillis = ttlMillis,
        )
    }

    fun exchange(ticket: String): AuthTokens? =
        redisTakeCache.take(key(ticket), AuthTokens::class.java)

    private fun key(ticket: String): String = "auth:ticket:$ticket"
}
