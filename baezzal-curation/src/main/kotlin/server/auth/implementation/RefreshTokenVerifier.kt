package server.auth.implementation

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
        val principal = tokenProvider.decodeToken(refreshToken)
        if (principal.type != TokenType.REFRESH) {
            throw InvalidTokenException()
        }

        val cachedRefreshToken = refreshTokenCache.get(principal.memberId)
        if (cachedRefreshToken != refreshToken) {
            throw InvalidTokenException()
        }

        return principal
    }
}
