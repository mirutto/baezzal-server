package server.auth.application

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import server.auth.implementation.Oauth2AuthorizationRequestIssuer
import server.auth.implementation.Oauth2ClientRegistrationReader
import server.auth.implementation.Oauth2StateManager

class Oauth2ServiceTest {
    private val oauth2ClientRegistrationReader = mockk<Oauth2ClientRegistrationReader>()
    private val oauth2StateManager = mockk<Oauth2StateManager>()
    private val oauth2AuthorizationRequestIssuer = mockk<Oauth2AuthorizationRequestIssuer>()
    private val oauth2Service = Oauth2Service(
        oauth2ClientRegistrationReader = oauth2ClientRegistrationReader,
        oauth2StateManager = oauth2StateManager,
        oauth2AuthorizationRequestIssuer = oauth2AuthorizationRequestIssuer,
    )

    @Test
    fun `oauth2 로그인 시작을 오케스트레이션한다`() {
        val clientRegistration = clientRegistration("google")
        val authorizationRequest = authorizationRequest()
        every { oauth2ClientRegistrationReader.read("google") } returns clientRegistration
        every { oauth2StateManager.issue("https://app.baezzal.com/login/callback") } returns "state-token"
        every {
            oauth2AuthorizationRequestIssuer.issue(
                clientRegistration = clientRegistration,
                state = "state-token",
                baseScheme = "https",
                baseHost = "api.baezzal.com",
                basePort = 443,
                basePath = "",
            )
        } returns authorizationRequest

        val result = oauth2Service.start(
            Oauth2LoginStartCommand(
                provider = "google",
                redirectUri = "https://app.baezzal.com/login/callback",
                baseScheme = "https",
                baseHost = "api.baezzal.com",
                basePort = 443,
                basePath = "",
            ),
        )

        result shouldBe "https://accounts.google.com/o/oauth2/v2/auth?state=state-token"
        verify(exactly = 1) { oauth2ClientRegistrationReader.read("google") }
        verify(exactly = 1) { oauth2StateManager.issue("https://app.baezzal.com/login/callback") }
        verify(exactly = 1) {
            oauth2AuthorizationRequestIssuer.issue(
                clientRegistration = clientRegistration,
                state = "state-token",
                baseScheme = "https",
                baseHost = "api.baezzal.com",
                basePort = 443,
                basePath = "",
            )
        }
    }

    @Test
    fun `state 로 redirect uri 를 1회 조회한다`() {
        every { oauth2StateManager.take("state-token") } returns "https://app.baezzal.com/login/callback"

        val result = oauth2Service.takeRedirectUri("state-token")

        result shouldBe "https://app.baezzal.com/login/callback"
        verify(exactly = 1) { oauth2StateManager.take("state-token") }
    }

    private fun clientRegistration(registrationId: String): ClientRegistration =
        ClientRegistration
            .withRegistrationId(registrationId)
            .clientId("client-id")
            .clientSecret("client-secret")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/api/v1/auth/callback/{registrationId}")
            .scope("profile", "email")
            .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
            .tokenUri("https://example.com/oauth/token")
            .userInfoUri("https://example.com/userinfo")
            .userNameAttributeName("id")
            .clientName(registrationId)
            .build()

    private fun authorizationRequest(): OAuth2AuthorizationRequest =
        OAuth2AuthorizationRequest.authorizationCode()
            .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
            .clientId("client-id")
            .redirectUri("https://api.baezzal.com/api/v1/auth/callback/google")
            .scopes(setOf("profile", "email"))
            .state("state-token")
            .attributes(mapOf("registration_id" to "google"))
            .authorizationRequestUri("https://accounts.google.com/o/oauth2/v2/auth?state=state-token")
            .build()
}
