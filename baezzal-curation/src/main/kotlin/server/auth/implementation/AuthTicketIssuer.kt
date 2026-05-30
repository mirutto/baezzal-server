package server.auth.implementation

import org.springframework.stereotype.Component
import server.auth.application.AuthTokenData
import server.auth.infrastructure.AuthTicketCache
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
            tokens = AuthTokenData(
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
