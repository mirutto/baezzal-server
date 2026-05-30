package server.auth.implementation

import org.springframework.stereotype.Component
import server.auth.domain.AuthTokens
import server.token.AuthPrincipal
import server.token.TokenProvider

@Component
class AuthTokenIssuer(
    private val tokenProvider: TokenProvider,
) {
    fun issue(
        memberId: Long,
        role: String,
    ): AuthTokens =
        AuthTokens(
            accessToken = tokenProvider.encodeToken(
                principal = AuthPrincipal.accessToken(memberId, role),
                ttl = ACCESS_TOKEN_TTL_MILLIS,
            ),
            refreshToken = tokenProvider.encodeToken(
                principal = AuthPrincipal.refreshToken(memberId),
                ttl = REFRESH_TOKEN_TTL_MILLIS,
            ),
        )

    companion object {
        private const val ACCESS_TOKEN_TTL_MILLIS = 60 * 60 * 1000L
        const val REFRESH_TOKEN_TTL_MILLIS = 7 * 24 * 60 * 60 * 1000L
    }
}
