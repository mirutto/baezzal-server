package server.auth.application

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.auth.application.AuthTokenData
import server.auth.implementation.AuthTicketExchanger
import server.auth.implementation.AuthTicketIssuer
import server.auth.implementation.AuthTokenIssuer
import server.auth.implementation.RefreshTokenVerifier
import server.auth.implementation.RefreshTokenWriter
import server.member.domain.Member
import server.member.domain.MemberProvider
import server.member.domain.MemberRole
import server.member.implementation.MemberReader
import server.member.implementation.MemberWriter
import server.token.AuthPrincipal
import server.token.TokenType

class AuthServiceTest {
    private val memberReader = mockk<MemberReader>()
    private val memberWriter = mockk<MemberWriter>()
    private val authTicketIssuer = mockk<AuthTicketIssuer>()
    private val authTicketExchanger = mockk<AuthTicketExchanger>()
    private val authTokenIssuer = mockk<AuthTokenIssuer>()
    private val refreshTokenVerifier = mockk<RefreshTokenVerifier>()
    private val refreshTokenWriter = mockk<RefreshTokenWriter>()
    private val authService = AuthService(
        memberReader = memberReader,
        memberWriter = memberWriter,
        authTicketIssuer = authTicketIssuer,
        authTicketExchanger = authTicketExchanger,
        authTokenIssuer = authTokenIssuer,
        refreshTokenVerifier = refreshTokenVerifier,
        refreshTokenWriter = refreshTokenWriter,
    )

    @Test
    fun `refresh token 으로 access token 과 refresh token 을 재발급한다`() {
        val refreshToken = "refresh-token"
        val member = Member(
            id = 1L,
            nickname = "tester",
            provider = MemberProvider.GOOGLE,
            providerKey = "provider-key",
            role = MemberRole.USER,
        )
        every { refreshTokenVerifier.verify(refreshToken) } returns AuthPrincipal(
            memberId = 1L,
            type = TokenType.REFRESH,
        )
        every { memberReader.readById(1L) } returns member
        every { authTokenIssuer.issue(1L, MemberRole.USER.name) } returns AuthTokenData(
            accessToken = "new-access-token",
            refreshToken = "new-refresh-token",
        )
        every {
            refreshTokenWriter.write(1L, "new-refresh-token")
        } just runs

        val result = authService.reissue(refreshToken)

        result shouldBe AuthTokenData(
            accessToken = "new-access-token",
            refreshToken = "new-refresh-token",
        )
        verify(exactly = 1) { refreshTokenVerifier.verify(refreshToken) }
        verify(exactly = 1) { memberReader.readById(1L) }
        verify(exactly = 1) { authTokenIssuer.issue(1L, MemberRole.USER.name) }
        verify(exactly = 1) { refreshTokenWriter.write(1L, "new-refresh-token") }
    }
}
