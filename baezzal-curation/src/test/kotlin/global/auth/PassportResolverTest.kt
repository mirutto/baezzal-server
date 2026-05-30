package global.auth

import global.error.UnauthorizedException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.core.MethodParameter
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.ServletWebRequest
import server.member.domain.MemberRole
import server.token.AuthPrincipal
import server.token.TokenProvider
import server.token.TokenType

class PassportResolverTest {
    private val tokenProvider = mockk<TokenProvider>()
    private val passportResolver = PassportResolver(tokenProvider)
    private val passportParameter = methodParameter("withPassport")
    private val plainPassportParameter = methodParameter("withoutAnnotation")
    private val stringParameter = methodParameter("withString")

    @Test
    fun `RequestPassport 와 Passport 조합이면 지원한다`() {
        val result = passportResolver.supportsParameter(passportParameter)

        result shouldBe true
    }

    @Test
    fun `RequestPassport 가 없으면 지원하지 않는다`() {
        val result = passportResolver.supportsParameter(plainPassportParameter)

        result shouldBe false
    }

    @Test
    fun `Passport 타입이 아니면 지원하지 않는다`() {
        val result = passportResolver.supportsParameter(stringParameter)

        result shouldBe false
    }

    @Test
    fun `access token 을 Passport 로 변환한다`() {
        val request = MockHttpServletRequest().apply {
            addHeader("Authorization", "Bearer access-token")
        }
        every { tokenProvider.decodeToken("access-token") } returns AuthPrincipal(
            memberId = 1L,
            type = TokenType.ACCESS,
            role = MemberRole.USER.name,
        )

        val result = passportResolver.resolveArgument(
            passportParameter,
            null,
            ServletWebRequest(request),
            null,
        )

        result shouldBe Passport(memberId = 1L, role = MemberRole.USER)
    }

    @Test
    fun `Authorization 헤더가 없으면 예외가 발생한다`() {
        val request = MockHttpServletRequest()

        shouldThrow<UnauthorizedException> {
            passportResolver.resolveArgument(
                passportParameter,
                null,
                ServletWebRequest(request),
                null,
            )
        }
    }

    @Test
    fun `refresh token 이면 예외가 발생한다`() {
        val request = MockHttpServletRequest().apply {
            addHeader("Authorization", "Bearer refresh-token")
        }
        every { tokenProvider.decodeToken("refresh-token") } returns AuthPrincipal(
            memberId = 1L,
            type = TokenType.REFRESH,
        )

        shouldThrow<UnauthorizedException> {
            passportResolver.resolveArgument(
                passportParameter,
                null,
                ServletWebRequest(request),
                null,
            )
        }
    }

    @Test
    fun `role 이 없으면 예외가 발생한다`() {
        val request = MockHttpServletRequest().apply {
            addHeader("Authorization", "Bearer access-token")
        }
        every { tokenProvider.decodeToken("access-token") } returns AuthPrincipal(
            memberId = 1L,
            type = TokenType.ACCESS,
            role = null,
        )

        shouldThrow<UnauthorizedException> {
            passportResolver.resolveArgument(
                passportParameter,
                null,
                ServletWebRequest(request),
                null,
            )
        }
    }

    private fun methodParameter(methodName: String): MethodParameter =
        MethodParameter(
            TestController::class.java.getDeclaredMethod(
                methodName,
                *methodParameterTypes(methodName),
            ),
            0,
        )

    private fun methodParameterTypes(methodName: String): Array<Class<*>> =
        when (methodName) {
            "withPassport", "withoutAnnotation" -> arrayOf(Passport::class.java)
            "withString" -> arrayOf(String::class.java)
            else -> error("unknown method: $methodName")
        }

    private class TestController {
        fun withPassport(@RequestPassport passport: Passport) = passport

        fun withoutAnnotation(passport: Passport) = passport

        fun withString(@RequestPassport value: String) = value
    }
}
