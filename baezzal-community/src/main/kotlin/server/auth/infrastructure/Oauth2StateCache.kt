package server.auth.infrastructure

import org.springframework.stereotype.Component
import server.cache.CacheMemory
import server.cache.RedisTakeCache

private const val OAUTH2_STATE_TTL_MILLIS = 5 * 60 * 1000L

@Component
class Oauth2StateCache(
    private val cacheMemory: CacheMemory,
    private val redisTakeCache: RedisTakeCache,
) {
    fun save(
        state: String,
        redirectUri: String,
    ) {
        cacheMemory.set(
            key = key(state),
            value = redirectUri,
            ttlMillis = OAUTH2_STATE_TTL_MILLIS,
        )
    }

    fun take(state: String): String? =
        redisTakeCache.take(key(state), String::class.java)

    private fun key(state: String): String = "auth:oauth2:state:$state"
}
