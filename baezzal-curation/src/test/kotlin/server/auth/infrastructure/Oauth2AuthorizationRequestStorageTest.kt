package server.auth.infrastructure

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import server.cache.CacheMemory
import server.cache.RedisTakeCache

class Oauth2AuthorizationRequestStorageTest {
    private val cacheMemory = mockk<CacheMemory>()
    private val redisTakeCache = mockk<RedisTakeCache>()
    private val oauth2AuthorizationRequestStorage = Oauth2AuthorizationRequestStorage(
        cacheMemory = cacheMemory,
        redisTakeCache = redisTakeCache,
    )

    @Test
    fun `authorization request 를 ttl 과 함께 저장한다`() {
        val authorizationRequest = authorizationRequest()
        every {
            cacheMemory.set(
                key = "auth:oauth2:authorization-request:state-token",
                value = Oauth2AuthorizationRequestCacheData(
                    authorizationUri = "https://accounts.google.com/o/oauth2/v2/auth",
                    clientId = "client-id",
                    redirectUri = "https://api.baezzal.com/api/v1/auth/callback/google",
                    scopes = setOf("profile", "email"),
                    state = "state-token",
                    additionalParameters = emptyMap(),
                    registrationId = "google",
                ),
                ttlMillis = 300_000L,
            )
        } returns Unit

        oauth2AuthorizationRequestStorage.save(authorizationRequest)

        verify(exactly = 1) {
            cacheMemory.set(
                key = "auth:oauth2:authorization-request:state-token",
                value = Oauth2AuthorizationRequestCacheData(
                    authorizationUri = "https://accounts.google.com/o/oauth2/v2/auth",
                    clientId = "client-id",
                    redirectUri = "https://api.baezzal.com/api/v1/auth/callback/google",
                    scopes = setOf("profile", "email"),
                    state = "state-token",
                    additionalParameters = emptyMap(),
                    registrationId = "google",
                ),
                ttlMillis = 300_000L,
            )
        }
    }

    @Test
    fun `state 로 authorization request 를 조회한다`() {
        every {
            cacheMemory.get(
                "auth:oauth2:authorization-request:state-token",
                Oauth2AuthorizationRequestCacheData::class.java,
            )
        } returns Oauth2AuthorizationRequestCacheData(
            authorizationUri = "https://accounts.google.com/o/oauth2/v2/auth",
            clientId = "client-id",
            redirectUri = "https://api.baezzal.com/api/v1/auth/callback/google",
            scopes = setOf("profile", "email"),
            state = "state-token",
            additionalParameters = emptyMap(),
            registrationId = "google",
        )

        val result = oauth2AuthorizationRequestStorage.load("state-token")

        result?.state shouldBe "state-token"
        result?.getAttribute<String>("registration_id") shouldBe "google"
    }

    @Test
    fun `state 로 authorization request 를 1회 조회한다`() {
        every {
            redisTakeCache.take(
                "auth:oauth2:authorization-request:state-token",
                Oauth2AuthorizationRequestCacheData::class.java,
            )
        } returns Oauth2AuthorizationRequestCacheData(
            authorizationUri = "https://accounts.google.com/o/oauth2/v2/auth",
            clientId = "client-id",
            redirectUri = "https://api.baezzal.com/api/v1/auth/callback/google",
            scopes = setOf("profile", "email"),
            state = "state-token",
            additionalParameters = emptyMap(),
            registrationId = "google",
        )

        val result = oauth2AuthorizationRequestStorage.take("state-token")

        result?.authorizationRequestUri shouldBe (
            "https://accounts.google.com/o/oauth2/v2/auth" +
                "?response_type=code" +
                "&client_id=client-id" +
                "&scope=profile%20email" +
                "&state=state-token" +
                "&redirect_uri=https://api.baezzal.com/api/v1/auth/callback/google"
        )
        verify(exactly = 1) {
            redisTakeCache.take(
                "auth:oauth2:authorization-request:state-token",
                Oauth2AuthorizationRequestCacheData::class.java,
            )
        }
    }

    private fun authorizationRequest(): OAuth2AuthorizationRequest =
        OAuth2AuthorizationRequest.authorizationCode()
            .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
            .clientId("client-id")
            .redirectUri("https://api.baezzal.com/api/v1/auth/callback/google")
            .scopes(setOf("profile", "email"))
            .state("state-token")
            .attributes(mapOf("registration_id" to "google"))
            .build()
}
