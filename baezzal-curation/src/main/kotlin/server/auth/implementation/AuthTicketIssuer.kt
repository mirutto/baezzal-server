package server.auth.implementation

import org.springframework.stereotype.Component
import server.auth.infrastructure.AuthTicketCache
import server.auth.domain.AuthTokens
import java.util.UUID

@Component
class AuthTicketIssuer(
    private val authTicketCache: AuthTicketCache,
) {
    fun issue(
        accessToken: String,
        refreshToken: String,
    ): String {
        val ticket = UUID.randomUUID().toString()
        authTicketCache.set(
            ticket = ticket,
            tokens = AuthTokens(
                accessToken = accessToken,
                refreshToken = refreshToken,
            ),
            ttlMillis = LOGIN_TICKET_TTL_MILLIS,
        )
        return ticket
    }

    companion object {
        private const val LOGIN_TICKET_TTL_MILLIS = 5 * 60 * 1000L
    }
}
