package server.auth.application

import global.error.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import server.auth.implementation.AuthTicketExchanger
import server.auth.implementation.AuthTicketIssuer
import server.auth.implementation.AuthTokenIssuer
import server.auth.implementation.RefreshTokenRemover
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
            provider = oauth2Attributes.provider,
            providerKey = oauth2Attributes.providerKey,
            profileImage = Member.DEFAULT_PROFILE_IMAGE_URL,
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
            memberId = memberId,
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
        )
    }

    fun exchangeTicket(ticket: String): AuthTicketExchangeResult {
        val payload = authTicketExchanger.exchange(ticket)
        val member = memberReader.readById(payload.memberId)
            ?: throw NotFoundException("회원을 찾을 수 없습니다")

        return AuthTicketExchangeResult(
            accessToken = payload.accessToken,
            refreshToken = payload.refreshToken,
            needsOnboarding = member.isNew(),
        )
    }

    fun reissue(refreshToken: String): AuthTokenResult {
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

        return AuthTokenResult(
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
        )
    }

    fun logout(refreshToken: String) {
        val principal = refreshTokenVerifier.verify(refreshToken)
        refreshTokenRemover.remove(principal.memberId)
    }
}
