package server.auth.implementation

import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import server.auth.infrastructure.Oauth2AuthorizationRequestStorage

@Component
class Oauth2AuthorizationRequestIssuer(
    private val oauth2AuthorizationRequestStorage: Oauth2AuthorizationRequestStorage,
) {
    fun issue(
        clientRegistration: ClientRegistration,
        state: String,
        baseScheme: String,
        baseHost: String,
        basePort: Int,
        basePath: String,
    ): OAuth2AuthorizationRequest {
        val redirectUri = resolveRedirectUri(
            clientRegistration = clientRegistration,
            baseScheme = baseScheme,
            baseHost = baseHost,
            basePort = basePort,
            basePath = basePath,
        )
        val authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
            .authorizationUri(clientRegistration.providerDetails.authorizationUri)
            .clientId(clientRegistration.clientId)
            .redirectUri(redirectUri)
            .scopes(clientRegistration.scopes ?: emptySet())
            .state(state)
            .attributes(
                mapOf(
                    "registration_id" to clientRegistration.registrationId,
                ),
            ).authorizationRequestUri(
                buildAuthorizationRequestUri(
                    clientRegistration = clientRegistration,
                    state = state,
                    redirectUri = redirectUri,
                ),
            ).build()

        oauth2AuthorizationRequestStorage.save(authorizationRequest)

        return authorizationRequest
    }

    private fun buildAuthorizationRequestUri(
        clientRegistration: ClientRegistration,
        state: String,
        redirectUri: String,
    ): String {
        val builder = UriComponentsBuilder
            .fromUriString(clientRegistration.providerDetails.authorizationUri)
            .queryParam(OAuth2ParameterNames.RESPONSE_TYPE, "code")
            .queryParam(OAuth2ParameterNames.CLIENT_ID, clientRegistration.clientId)
            .queryParam(OAuth2ParameterNames.REDIRECT_URI, redirectUri)
            .queryParam(OAuth2ParameterNames.STATE, state)

        clientRegistration.scopes
            ?.toSet()
            ?.takeIf { it.isNotEmpty() }
            ?.joinToString(" ")
            ?.let { builder.queryParam(OAuth2ParameterNames.SCOPE, it) }

        return builder
            .build()
            .encode()
            .toUriString()
    }

    private fun resolveRedirectUri(
        clientRegistration: ClientRegistration,
        baseScheme: String,
        baseHost: String,
        basePort: Int,
        basePath: String,
    ): String {
        val baseUrlBuilder = UriComponentsBuilder
            .newInstance()
            .scheme(baseScheme)
            .host(baseHost)
            .path(basePath)

        if (!isDefaultPort(basePort)) {
            baseUrlBuilder.port(basePort)
        }

        val baseUrl = baseUrlBuilder.build().toUriString()

        return clientRegistration.redirectUri
            .replace("{baseUrl}", baseUrl)
            .replace("{baseScheme}", baseScheme)
            .replace("{baseHost}", baseHost)
            .replace("{basePort}", basePort.toString())
            .replace("{basePath}", basePath)
            .replace("{registrationId}", clientRegistration.registrationId)
    }

    private fun isDefaultPort(port: Int): Boolean = port == 80 || port == 443
}
