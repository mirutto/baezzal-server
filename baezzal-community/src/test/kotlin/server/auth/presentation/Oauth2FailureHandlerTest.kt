package server.auth.presentation

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.AuthenticationException
import server.auth.application.Oauth2Service

class Oauth2FailureHandlerTest {
    private val oauth2Service = mockk<Oauth2Service>()
    private val failureHandler = Oauth2FailureHandler(oauth2Service)

    @Test
    fun `인증 실패 시 state 로 redirect uri 를 조회해 에러 redirect 한다`() {
        every {
            oauth2Service.takeRedirectUri("state-token")
        } returns "https://app.baezzal.com/login/callback"
        val request = MockHttpServletRequest().apply {
            addParameter("state", "state-token")
        }
        val response = MockHttpServletResponse()

        failureHandler.onAuthenticationFailure(
            request,
            response,
            object : AuthenticationException("oauth denied") {},
        )

        response.status shouldBe 302
        response.redirectedUrl shouldBe (
            "https://app.baezzal.com/login/callback" +
                "?type=hasError" +
                "&errorMessage=oauth%20denied"
        )
        verify(exactly = 1) { oauth2Service.takeRedirectUri("state-token") }
    }

    @Test
    fun `만료된 state 이면 401 에러를 반환한다`() {
        every { oauth2Service.takeRedirectUri("expired-state") } returns null
        val request = MockHttpServletRequest().apply {
            addParameter("state", "expired-state")
        }
        val response = MockHttpServletResponse()

        failureHandler.onAuthenticationFailure(
            request,
            response,
            object : AuthenticationException("oauth denied") {},
        )

        response.status shouldBe 401
        response.errorMessage shouldBe "로그인 상태가 유효하지 않습니다"
        verify(exactly = 1) { oauth2Service.takeRedirectUri("expired-state") }
    }
}
