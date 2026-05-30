package server.auth.implementation

import org.springframework.stereotype.Component
import server.auth.infrastructure.RefreshTokenCache

@Component
class RefreshTokenWriter(
    private val refreshTokenCache: RefreshTokenCache,
) {
    fun write(
        memberId: Long,
        refreshToken: String,
    ) {
        refreshTokenCache.set(
            memberId = memberId,
            refreshToken = refreshToken,
            ttlMillis = AuthTokenIssuer.REFRESH_TOKEN_TTL_MILLIS,
        )
    }
}
