package server.auth.presentation

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.Authentication
import server.auth.application.AuthService
import server.auth.application.Oauth2Service

class Oauth2SuccessHandlerTest {
    private val authService = mockk<AuthService>()
    private val oauth2Service = mockk<Oauth2Service>()
    private val successHandler = Oauth2SuccessHandler(
        authService = authService,
        oauth2Service = oauth2Service,
    )

    @Test
    fun `인증 성공 시 state 로 redirect uri 를 조회해 ticket 과 함께 redirect 한다`() {
        every {
            oauth2Service.takeRedirectUri("state-token")
        } returns "https://app.baezzal.com/login/callback"
        every { authService.issueLoginTicket(1L, "USER") } returns "ticket"
        val authentication = mockk<Authentication>()
        every { authentication.principal } returns Oauth2SocialUser.Authenticated(
            memberId = 1L,
            role = "USER",
        )
        val request = MockHttpServletRequest().apply {
            addParameter("state", "state-token")
        }
        val response = MockHttpServletResponse()

        successHandler.onAuthenticationSuccess(request, response, authentication)

        response.status shouldBe 302
        response.redirectedUrl shouldBe "https://app.baezzal.com/login/callback?type=success&ticket=ticket"
        verify(exactly = 1) { oauth2Service.takeRedirectUri("state-token") }
        verify(exactly = 1) { authService.issueLoginTicket(1L, "USER") }
    }

    @Test
    fun `OAuth 사용자 정보 처리 실패 시 에러 redirect 한다`() {
        every {
            oauth2Service.takeRedirectUri("state-token")
        } returns "https://app.baezzal.com/login/callback"
        val authentication = mockk<Authentication>()
        every { authentication.principal } returns Oauth2SocialUser.HasError("nickname is missing")
        val request = MockHttpServletRequest().apply {
            addParameter("state", "state-token")
        }
        val response = MockHttpServletResponse()

        successHandler.onAuthenticationSuccess(request, response, authentication)

        response.redirectedUrl shouldBe (
            "https://app.baezzal.com/login/callback" +
                "?type=hasError" +
                "&errorMessage=nickname%20is%20missing"
        )
    }

    @Test
    fun `state 가 없으면 401 에러를 반환한다`() {
        val authentication = mockk<Authentication>()
        every { authentication.principal } returns Oauth2SocialUser.Authenticated(
            memberId = 1L,
            role = "USER",
        )
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()

        successHandler.onAuthenticationSuccess(request, response, authentication)

        response.status shouldBe 401
        response.errorMessage shouldBe "로그인 상태가 유효하지 않습니다"
    }

    @Test
    fun `재사용된 state 이면 401 에러를 반환한다`() {
        every { oauth2Service.takeRedirectUri("used-state") } returns null
        val authentication = mockk<Authentication>()
        every { authentication.principal } returns Oauth2SocialUser.Authenticated(
            memberId = 1L,
            role = "USER",
        )
        val request = MockHttpServletRequest().apply {
            addParameter("state", "used-state")
        }
        val response = MockHttpServletResponse()

        successHandler.onAuthenticationSuccess(request, response, authentication)

        response.status shouldBe 401
        response.errorMessage shouldBe "로그인 상태가 유효하지 않습니다"
        verify(exactly = 1) { oauth2Service.takeRedirectUri("used-state") }
    }
}
