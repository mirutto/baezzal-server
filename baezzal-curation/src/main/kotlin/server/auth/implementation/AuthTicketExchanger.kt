package server.auth.implementation

import org.springframework.stereotype.Component
import server.auth.application.AuthTokenData
import server.auth.infrastructure.AuthTicketCache

@Component
class AuthTicketExchanger(
    private val authTicketCache: AuthTicketCache,
) {
    fun exchange(ticket: String): AuthTokenData =
        authTicketCache.exchange(ticket)
            ?: throw IllegalArgumentException("유효하지 않은 인증 ticket 입니다")
}
