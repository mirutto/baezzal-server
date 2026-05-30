package server.auth.implementation

import global.error.BadRequestException
import org.springframework.stereotype.Component
import server.auth.domain.AuthTokens
import server.auth.infrastructure.AuthTicketCache

@Component
class AuthTicketExchanger(
    private val authTicketCache: AuthTicketCache,
) {
    fun exchange(ticket: String): AuthTokens =
        authTicketCache.exchange(ticket)
            ?: throw BadRequestException("유효하지 않은 인증 ticket 입니다")
}
