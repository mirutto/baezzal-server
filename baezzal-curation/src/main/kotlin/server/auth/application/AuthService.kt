package server.auth.application

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import server.auth.implementation.AuthTicketExchanger
import server.auth.implementation.AuthTicketIssuer
import server.auth.implementation.AuthTokenIssuer
import server.auth.implementation.RefreshTokenVerifier
import server.auth.implementation.RefreshTokenWriter
import server.member.domain.Member
import server.member.domain.MemberRole
import server.member.implementation.MemberReader
import server.member.implementation.MemberWriter

@Service
class AuthService(
    private val memberReader: MemberReader,
    private val memberWriter: MemberWriter,
    private val authTicketIssuer: AuthTicketIssuer,
    private val authTicketExchanger: AuthTicketExchanger,
    private val authTokenIssuer: AuthTokenIssuer,
    private val refreshTokenVerifier: RefreshTokenVerifier,
    private val refreshTokenWriter: RefreshTokenWriter,
) {

    @Transactional
    fun upsert(oauth2Attributes: Oauth2Attributes): MemberPrincipal {
        memberReader.readByProvider(
            oauth2Attributes.provider,
            oauth2Attributes.providerKey
        )?.let {
            return MemberPrincipal(
                memberId = it.id,
                role = it.role,
            )
        }

        val member = Member(
            nickname = "",
            provider = oauth2Attributes.provider,
            providerKey = oauth2Attributes.providerKey,
        )
        val saved = memberWriter.write(member)

        return MemberPrincipal(
            memberId = saved.id,
            role = saved.role,
        )
    }

    fun issueLoginTicket(
        memberId: Long,
        role: MemberRole,
    ): String {
        val tokens = authTokenIssuer.issue(
            memberId = memberId,
            role = role.name,
        )
        refreshTokenWriter.write(
            memberId = memberId,
            refreshToken = tokens.refreshToken,
        )

        return authTicketIssuer.issue(
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
        )
    }

    fun exchangeTicket(ticket: String): AuthTokenData {
        return authTicketExchanger.exchange(ticket)
    }

    fun reissue(refreshToken: String): AuthTokenData {
        val principal = refreshTokenVerifier.verify(refreshToken)
        val member = memberReader.readById(principal.memberId)
            ?: throw NoSuchElementException("회원을 찾을 수 없습니다")
        val tokens = authTokenIssuer.issue(
            memberId = member.id,
            role = member.role.name,
        )
        refreshTokenWriter.write(
            memberId = member.id,
            refreshToken = tokens.refreshToken,
        )

        return AuthTokenData(
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
        )
    }
}
