package server.auth.implementation

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.core.AuthorizationGrantType
import server.auth.infrastructure.Oauth2AuthorizationRequestStorage
import java.net.URI

class Oauth2AuthorizationRequestIssuerTest {
    private val oauth2AuthorizationRequestStorage = mockk<Oauth2AuthorizationRequestStorage>(relaxed = true)
    private val oauth2AuthorizationRequestIssuer = Oauth2AuthorizationRequestIssuer(oauth2AuthorizationRequestStorage)

    @Test
    fun `scope 가 있는 provider 는 authorize url 을 생성하고 저장한다`() {
        val result = oauth2AuthorizationRequestIssuer.issue(
            clientRegistration = clientRegistration(
                registrationId = "google",
                authorizationUri = "https://accounts.google.com/o/oauth2/v2/auth",
                scopes = listOf("profile", "email"),
            ),
            state = "state-token",
            baseScheme = "https",
            baseHost = "api.baezzal.com",
            basePort = 443,
            basePath = "",
        )

        result.authorizationRequestUri shouldBe (
            "https://accounts.google.com/o/oauth2/v2/auth" +
                "?response_type=code" +
                "&client_id=client-id" +
                "&redirect_uri=https://api.baezzal.com/api/v1/auth/callback/google" +
                "&state=state-token" +
                "&scope=profile%20email"
        )
        verify(exactly = 1) { oauth2AuthorizationRequestStorage.save(result) }
    }

    @Test
    fun `scope 가 없는 provider 는 scope 파라미터 없이 authorize url 을 생성하고 저장한다`() {
        val result = oauth2AuthorizationRequestIssuer.issue(
            clientRegistration = clientRegistration(
                registrationId = "naver",
                authorizationUri = "https://nid.naver.com/oauth2.0/authorize",
                scopes = emptyList(),
            ),
            state = "state-token",
            baseScheme = "https",
            baseHost = "api.baezzal.com",
            basePort = 443,
            basePath = "",
        )

        URI.create(result.authorizationRequestUri).query shouldBe (
            "response_type=code" +
                "&client_id=client-id" +
                "&redirect_uri=https://api.baezzal.com/api/v1/auth/callback/naver" +
                "&state=state-token"
        )
        verify(exactly = 1) { oauth2AuthorizationRequestStorage.save(result) }
    }

    private fun clientRegistration(
        registrationId: String,
        authorizationUri: String,
        scopes: List<String>,
    ): ClientRegistration =
        ClientRegistration
            .withRegistrationId(registrationId)
            .clientId("client-id")
            .clientSecret("client-secret")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/api/v1/auth/callback/{registrationId}")
            .scope(scopes)
            .authorizationUri(authorizationUri)
            .tokenUri("https://example.com/oauth/token")
            .userInfoUri("https://example.com/userinfo")
            .userNameAttributeName("id")
            .clientName(registrationId)
            .build()
}
