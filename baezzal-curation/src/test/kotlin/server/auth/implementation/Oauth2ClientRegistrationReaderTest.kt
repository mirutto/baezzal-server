package server.auth.implementation

import global.error.BadRequestException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType

class Oauth2ClientRegistrationReaderTest {
    private val clientRegistrationRepository = mockk<ClientRegistrationRepository>()
    private val oauth2ClientRegistrationReader = Oauth2ClientRegistrationReader(clientRegistrationRepository)

    @Test
    fun `provider 로 client registration 을 조회한다`() {
        every { clientRegistrationRepository.findByRegistrationId("google") } returns clientRegistration("google")

        val result = oauth2ClientRegistrationReader.read("google")

        result.registrationId shouldBe "google"
    }

    @Test
    fun `지원하지 않는 provider 이면 예외가 발생한다`() {
        every { clientRegistrationRepository.findByRegistrationId("unknown") } returns null

        val result = shouldThrow<BadRequestException> {
            oauth2ClientRegistrationReader.read("unknown")
        }

        result.message shouldBe "지원하지 않는 OAuth provider 입니다"
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
}
