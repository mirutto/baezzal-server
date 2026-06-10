package server.auth.infrastructure

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.cache.CacheMemory
import server.cache.RedisTakeCache

class Oauth2StateCacheTest {
    private val cacheMemory = mockk<CacheMemory>()
    private val redisTakeCache = mockk<RedisTakeCache>()
    private val oauth2StateCache = Oauth2StateCache(
        cacheMemory = cacheMemory,
        redisTakeCache = redisTakeCache,
    )

    @Test
    fun `state 와 redirect uri 를 ttl 과 함께 저장한다`() {
        every {
            cacheMemory.set(
                key = "auth:oauth2:state:state-token",
                value = "https://app.baezzal.com/login/callback",
                ttlMillis = 300_000L,
            )
        } returns Unit

        oauth2StateCache.save("state-token", "https://app.baezzal.com/login/callback")

        verify(exactly = 1) {
            cacheMemory.set(
                key = "auth:oauth2:state:state-token",
                value = "https://app.baezzal.com/login/callback",
                ttlMillis = 300_000L,
            )
        }
    }

    @Test
    fun `state 로 redirect uri 를 1회 조회한다`() {
        every {
            redisTakeCache.take("auth:oauth2:state:state-token", String::class.java)
        } returns "https://app.baezzal.com/login/callback"

        oauth2StateCache.take("state-token")

        verify(exactly = 1) {
            redisTakeCache.take("auth:oauth2:state:state-token", String::class.java)
        }
    }
}
