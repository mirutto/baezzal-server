package server.auth.application

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import server.auth.implementation.AuthTicketExchanger
import server.auth.implementation.AuthTicketIssuer
import server.auth.implementation.AuthTokenIssuer
import server.auth.implementation.RefreshTokenRemover
import server.auth.implementation.RefreshTokenVerifier
import server.auth.implementation.RefreshTokenWriter
import server.member.domain.Member
import server.member.implementation.MemberEventPublisher
import server.member.implementation.MemberReader
import server.member.implementation.MemberWriter
import java.util.UUID

@Service
class AuthService(
    private val memberReader: MemberReader,
    private val memberWriter: MemberWriter,
    private val memberEventPublisher: MemberEventPublisher,
    private val authTicketIssuer: AuthTicketIssuer,
    private val authTicketExchanger: AuthTicketExchanger,
    private val authTokenIssuer: AuthTokenIssuer,
    private val refreshTokenVerifier: RefreshTokenVerifier,
    private val refreshTokenWriter: RefreshTokenWriter,
    private val refreshTokenRemover: RefreshTokenRemover,
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
            username = UUID.randomUUID().toString(),
            provider = oauth2Attributes.provider,
            providerKey = oauth2Attributes.providerKey,
            profileImage = Member.defaultProfileImage(),
            description = "",
        )
        val saved = memberWriter.write(member)

        memberEventPublisher.publishCreated(saved.id)

        return Oauth2LoginResult(
            memberId = saved.id,
            role = saved.role.name,
        )
    }

    fun issueLoginTicket(
        memberId: Long,
        role: String,
    ): String {
        val sessionId = UUID.randomUUID().toString()
        val tokens = authTokenIssuer.issue(
            memberId = memberId,
            role = role,
            sessionId = sessionId,
        )
        refreshTokenWriter.write(
            sessionId = sessionId,
            refreshToken = tokens.refreshToken,
        )

        return authTicketIssuer.issue(
            memberId = memberId,
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
        )
    }

    fun exchangeTicket(ticket: String): AuthTicketExchangeResult {
        val payload = authTicketExchanger.exchange(ticket)
        val member = memberReader.readById(payload.memberId)

        return AuthTicketExchangeResult(
            accessToken = payload.accessToken,
            refreshToken = payload.refreshToken,
            needsOnboarding = member.isNew(),
        )
    }

    fun reissue(refreshToken: String): AuthTokenResult {
        val principal = refreshTokenVerifier.verify(refreshToken)
        val member = memberReader.readById(principal.memberId)
        val sessionId = principal.sessionId ?: error("refresh token sessionId is required")
        val tokens = authTokenIssuer.issue(
            memberId = member.id,
            role = member.role.name,
            sessionId = sessionId,
        )
        refreshTokenWriter.write(
            sessionId = sessionId,
            refreshToken = tokens.refreshToken,
        )

        return AuthTokenResult(
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
        )
    }

    fun logout(refreshToken: String) {
        val principal = refreshTokenVerifier.verify(refreshToken)
        val sessionId = principal.sessionId ?: error("refresh token sessionId is required")
        refreshTokenRemover.remove(sessionId)
    }
}
