package server.auth.presentation

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import server.auth.application.AuthService
import server.auth.application.Oauth2LoginStartCommand
import server.auth.application.Oauth2Service

class AuthControllerTest {
    private val authService = mockk<AuthService>()
    private val oauth2Service = mockk<Oauth2Service>()
    private val authController = AuthController(
        authService = authService,
        oauth2Service = oauth2Service,
    )

    @Test
    fun `oauth2 로그인 시작 시 service 가 반환한 authorize url 로 redirect 한다`() {
        every {
            oauth2Service.start(
                Oauth2LoginStartCommand(
                    provider = "google",
                    redirectUri = "https://app.baezzal.com/login/callback",
                    baseScheme = "https",
                    baseHost = "api.baezzal.com",
                    basePort = 443,
                    basePath = "",
                ),
            )
        } returns "https://accounts.google.com/o/oauth2/v2/auth?state=state-token"
        val request = MockHttpServletRequest().apply {
            scheme = "https"
            serverName = "api.baezzal.com"
            serverPort = 443
        }
        val response = MockHttpServletResponse()

        authController.startOauth2Login(
            provider = "google",
            redirectUri = "https://app.baezzal.com/login/callback",
            request = request,
            response = response,
        )

        response.status shouldBe HttpServletResponse.SC_FOUND
        response.redirectedUrl shouldBe "https://accounts.google.com/o/oauth2/v2/auth?state=state-token"
        verify(exactly = 1) {
            oauth2Service.start(
                Oauth2LoginStartCommand(
                    provider = "google",
                    redirectUri = "https://app.baezzal.com/login/callback",
                    baseScheme = "https",
                    baseHost = "api.baezzal.com",
                    basePort = 443,
                    basePath = "",
                ),
            )
        }
    }
}
