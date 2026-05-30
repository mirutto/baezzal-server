package server.auth.application

import global.error.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import server.auth.implementation.AuthTicketExchanger
import server.auth.implementation.AuthTicketIssuer
import server.auth.implementation.AuthTokenIssuer
import server.auth.implementation.RefreshTokenVerifier
import server.auth.implementation.RefreshTokenWriter
import server.member.domain.Member
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
    fun upsert(
        registrationId: String,
        attributes: Map<String, Any>,
    ): Oauth2LoginResult {
        val oauth2Attributes = Oauth2Attributes.from(registrationId, attributes)

        memberReader.readByProvider(
            oauth2Attributes.provider,
            oauth2Attributes.providerKey,
        )?.let {
            return Oauth2LoginResult(
                memberId = it.id,
                role = it.role.name,
            )
        }

        val member = Member(
            nickname = "",
            provider = oauth2Attributes.provider,
            providerKey = oauth2Attributes.providerKey,
        )
        val saved = memberWriter.write(member)

        return Oauth2LoginResult(
            memberId = saved.id,
            role = saved.role.name,
        )
    }

    fun issueLoginTicket(
        memberId: Long,
        role: String,
    ): String {
        val tokens = authTokenIssuer.issue(
            memberId = memberId,
            role = role,
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
        val tokens = authTicketExchanger.exchange(ticket)

        return AuthTokenData(
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
        )
    }

    fun reissue(refreshToken: String): AuthTokenData {
        val principal = refreshTokenVerifier.verify(refreshToken)
        val member = memberReader.readById(principal.memberId)
            ?: throw NotFoundException("회원을 찾을 수 없습니다")
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
