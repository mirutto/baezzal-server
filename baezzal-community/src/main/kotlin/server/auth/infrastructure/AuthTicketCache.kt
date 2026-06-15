package server.auth.infrastructure

import org.springframework.stereotype.Component
import server.auth.domain.AuthTicketPayload
import server.cache.CacheMemory
import server.cache.RedisTakeCache

@Component
class AuthTicketCache(
    private val cacheMemory: CacheMemory,
    private val redisTakeCache: RedisTakeCache,
) {
    fun set(
        ticket: String,
        payload: AuthTicketPayload,
        ttlMillis: Long,
    ) {
        cacheMemory.set(
            key = key(ticket),
            value = payload,
            ttlMillis = ttlMillis,
        )
    }

    fun exchange(ticket: String): AuthTicketPayload? =
        redisTakeCache.take(key(ticket), AuthTicketPayload::class.java)

    private fun key(ticket: String): String = "auth:ticket:$ticket"
}
