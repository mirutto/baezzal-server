package server.auth.implementation

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import server.auth.infrastructure.RefreshTokenCache
import server.token.AuthPrincipal
import server.token.InvalidTokenException
import server.token.TokenProvider
import server.token.TokenType

class RefreshTokenVerifierTest {
    private val tokenProvider = mockk<TokenProvider>()
    private val refreshTokenCache = mockk<RefreshTokenCache>()
    private val refreshTokenVerifier = RefreshTokenVerifier(tokenProvider, refreshTokenCache)

    @Test
    fun `refresh token 이고 캐시와 일치하면 principal 을 반환한다`() {
        val refreshToken = "refresh-token"
        val principal = AuthPrincipal(memberId = 1L, type = TokenType.REFRESH)
        every { tokenProvider.decodeToken(refreshToken) } returns principal
        every { refreshTokenCache.get(1L) } returns refreshToken

        val result = refreshTokenVerifier.verify(refreshToken)

        result shouldBe principal
    }

    @Test
    fun `access token 이면 예외가 발생한다`() {
        val refreshToken = "access-token"
        every {
            tokenProvider.decodeToken(refreshToken)
        } returns AuthPrincipal(memberId = 1L, type = TokenType.ACCESS, role = "USER")

        shouldThrow<InvalidTokenException> {
            refreshTokenVerifier.verify(refreshToken)
        }
    }

    @Test
    fun `캐시의 refresh token 과 다르면 예외가 발생한다`() {
        val refreshToken = "refresh-token"
        every {
            tokenProvider.decodeToken(refreshToken)
        } returns AuthPrincipal(memberId = 1L, type = TokenType.REFRESH)
        every { refreshTokenCache.get(1L) } returns "different-token"

        shouldThrow<InvalidTokenException> {
            refreshTokenVerifier.verify(refreshToken)
        }
    }
}
