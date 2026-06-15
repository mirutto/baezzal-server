package server.auth.implementation

import global.error.UnauthorizedException
import org.springframework.stereotype.Component
import server.auth.infrastructure.RefreshTokenCache
import server.token.AuthPrincipal
import server.token.ExpiredTokenException
import server.token.InvalidTokenException
import server.token.TokenProvider
import server.token.TokenType

@Component
class RefreshTokenVerifier(
    private val tokenProvider: TokenProvider,
    private val refreshTokenCache: RefreshTokenCache,
) {
    fun verify(refreshToken: String): AuthPrincipal {
        val principal = decodeRefreshToken(refreshToken)
        if (principal.type != TokenType.REFRESH) {
            invalidToken()
        }

        val sessionId = principal.sessionId ?: invalidToken()
        val cachedRefreshToken = refreshTokenCache.get(sessionId)
        if (cachedRefreshToken != refreshToken) {
            invalidToken()
        }

        return principal
    }

    private fun decodeRefreshToken(refreshToken: String): AuthPrincipal =
        try {
            tokenProvider.decodeToken(refreshToken)
        } catch (_: InvalidTokenException) {
            invalidToken()
        } catch (_: ExpiredTokenException) {
            expiredToken()
        }

    private fun invalidToken(): Nothing = throw UnauthorizedException("LOGIN_AGAIN")

    private fun expiredToken(): Nothing = throw UnauthorizedException("TOKEN_EXPIRED")
}
