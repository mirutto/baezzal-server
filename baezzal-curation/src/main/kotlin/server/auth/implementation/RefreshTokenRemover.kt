package server.auth.implementation

import org.springframework.stereotype.Component
import server.auth.infrastructure.RefreshTokenCache

@Component
class RefreshTokenRemover(
    private val refreshTokenCache: RefreshTokenCache,
) {
    fun remove(memberId: Long) {
        refreshTokenCache.delete(memberId)
    }
}
