package server.auth.implementation

import global.error.UnauthorizedException
import org.springframework.stereotype.Component
import server.auth.infrastructure.RefreshTokenCache
import server.token.AuthPrincipal
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

        val cachedRefreshToken = refreshTokenCache.get(principal.memberId)
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
        }

    private fun invalidToken(): Nothing = throw UnauthorizedException("INVALID_TOKEN")
}
