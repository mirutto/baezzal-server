package server.auth.infrastructure

import org.springframework.stereotype.Component
import server.auth.application.AuthTokenData
import server.cache.RedisCache
import server.cache.RedisTakeCache

@Component
class AuthTicketCache(
    private val redisCache: RedisCache,
    private val redisTakeCache: RedisTakeCache,
) {
    fun set(
        ticket: String,
        tokens: AuthTokenData,
        ttlMillis: Long,
    ) {
        redisCache.set(
            key = key(ticket),
            value = tokens,
            ttlMillis = ttlMillis,
        )
    }

    fun exchange(ticket: String): AuthTokenData? =
        redisTakeCache.take(key(ticket), AuthTokenData::class.java)

    private fun key(ticket: String): String = "auth:ticket:$ticket"
}
