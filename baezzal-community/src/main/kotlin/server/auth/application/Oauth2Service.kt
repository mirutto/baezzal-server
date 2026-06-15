package server.auth.application

import org.springframework.stereotype.Service
import server.auth.implementation.Oauth2AuthorizationRequestIssuer
import server.auth.implementation.Oauth2ClientRegistrationReader
import server.auth.implementation.Oauth2StateManager

@Service
class Oauth2Service(
    private val oauth2ClientRegistrationReader: Oauth2ClientRegistrationReader,
    private val oauth2StateManager: Oauth2StateManager,
    private val oauth2AuthorizationRequestIssuer: Oauth2AuthorizationRequestIssuer,
) {
    fun start(command: Oauth2LoginStartCommand): String {
        val clientRegistration = oauth2ClientRegistrationReader.read(command.provider)
        val state = oauth2StateManager.issue(command.redirectUri)
        val authorizationRequest = oauth2AuthorizationRequestIssuer.issue(
            clientRegistration = clientRegistration,
            state = state,
            baseScheme = command.baseScheme,
            baseHost = command.baseHost,
            basePort = command.basePort,
            basePath = command.basePath,
        )

        return authorizationRequest.authorizationRequestUri
    }

    fun takeRedirectUri(state: String): String? = oauth2StateManager.take(state)
}
