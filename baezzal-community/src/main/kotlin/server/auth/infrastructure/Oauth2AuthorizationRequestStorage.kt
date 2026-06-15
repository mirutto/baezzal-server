package server.auth.infrastructure

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.stereotype.Component
import server.cache.CacheMemory
import server.cache.RedisTakeCache

private const val OAUTH2_AUTHORIZATION_REQUEST_TTL_MILLIS = 5 * 60 * 1000L

@Component
class Oauth2AuthorizationRequestStorage(
    private val cacheMemory: CacheMemory,
    private val redisTakeCache: RedisTakeCache,
) : AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    override fun loadAuthorizationRequest(request: HttpServletRequest): OAuth2AuthorizationRequest? =
        request.getParameter("state")
            ?.takeIf { it.isNotBlank() }
            ?.let(::load)

    override fun saveAuthorizationRequest(
        authorizationRequest: OAuth2AuthorizationRequest?,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        if (authorizationRequest == null) {
            request.getParameter("state")
                ?.takeIf { it.isNotBlank() }
                ?.let { cacheMemory.evict(key(it)) }
            return
        }

        save(authorizationRequest)
    }

    override fun removeAuthorizationRequest(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): OAuth2AuthorizationRequest? =
        request.getParameter("state")
            ?.takeIf { it.isNotBlank() }
            ?.let(::take)

    fun save(authorizationRequest: OAuth2AuthorizationRequest) {
        val state = authorizationRequest.state ?: return
        cacheMemory.set(
            key = key(state),
            value = authorizationRequest.toCacheData(),
            ttlMillis = OAUTH2_AUTHORIZATION_REQUEST_TTL_MILLIS,
        )
    }

    fun load(state: String): OAuth2AuthorizationRequest? =
        cacheMemory.get(key(state), Oauth2AuthorizationRequestCacheData::class.java)
            ?.toAuthorizationRequest()

    fun take(state: String): OAuth2AuthorizationRequest? =
        redisTakeCache.take(key(state), Oauth2AuthorizationRequestCacheData::class.java)
            ?.toAuthorizationRequest()

    private fun key(state: String): String = "auth:oauth2:authorization-request:$state"

    private fun OAuth2AuthorizationRequest.toCacheData(): Oauth2AuthorizationRequestCacheData =
        Oauth2AuthorizationRequestCacheData(
            authorizationUri = authorizationUri,
            clientId = clientId,
            redirectUri = redirectUri,
            scopes = scopes ?: emptySet(),
            state = state,
            additionalParameters = additionalParameters.mapValues { (_, value) -> value.toString() },
            registrationId = getAttribute<String>("registration_id")
                ?: error("OAuth2 authorization request registration_id attribute is required"),
        )

    private fun Oauth2AuthorizationRequestCacheData.toAuthorizationRequest(): OAuth2AuthorizationRequest =
        OAuth2AuthorizationRequest.authorizationCode()
            .authorizationUri(authorizationUri)
            .clientId(clientId)
            .redirectUri(redirectUri)
            .scopes(scopes)
            .state(state)
            .additionalParameters(additionalParameters.mapValues { (_, value) -> value as Any })
            .attributes(
                mapOf(
                    "registration_id" to registrationId,
                ),
            ).build()
}
