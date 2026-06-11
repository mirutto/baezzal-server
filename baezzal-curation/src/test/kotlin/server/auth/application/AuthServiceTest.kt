package server.auth.application

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.auth.domain.AuthTicketPayload
import server.auth.implementation.AuthTicketExchanger
import server.auth.implementation.AuthTicketIssuer
import server.auth.implementation.AuthTokenIssuer
import server.auth.implementation.RefreshTokenRemover
import server.auth.implementation.RefreshTokenVerifier
import server.auth.implementation.RefreshTokenWriter
import server.auth.domain.AuthTokens
import server.member.domain.Member
import server.member.domain.MemberProvider
import server.member.domain.MemberRole
import server.member.implementation.MemberReader
import server.member.implementation.MemberWriter
import server.token.AuthPrincipal
import server.token.TokenType

class AuthServiceTest {
    private val memberReader = mockk<MemberReader>()
    private val memberWriter = mockk<MemberWriter>()
    private val authTicketIssuer = mockk<AuthTicketIssuer>()
    private val authTicketExchanger = mockk<AuthTicketExchanger>()
    private val authTokenIssuer = mockk<AuthTokenIssuer>()
    private val refreshTokenVerifier = mockk<RefreshTokenVerifier>()
    private val refreshTokenWriter = mockk<RefreshTokenWriter>()
    private val refreshTokenRemover = mockk<RefreshTokenRemover>()
    private val authService = AuthService(
        memberReader = memberReader,
        memberWriter = memberWriter,
        authTicketIssuer = authTicketIssuer,
        authTicketExchanger = authTicketExchanger,
        authTokenIssuer = authTokenIssuer,
        refreshTokenVerifier = refreshTokenVerifier,
        refreshTokenWriter = refreshTokenWriter,
        refreshTokenRemover = refreshTokenRemover,
    )

    @Test
    fun `ticket 을 교환할 때 닉네임이 비어 있으면 온보딩 필요 여부를 함께 반환한다`() {
        val accessToken = "access-token"
        val refreshToken = "refresh-token"
        val member = Member(
            id = 1L,
            nickname = "",
            username = "11111111-1111-1111-1111-111111111111",
            provider = MemberProvider.GOOGLE,
            providerKey = "provider-key",
            profileImage = "",
            description = "",
            preferredTeamId = null,
            role = MemberRole.USER,
        )
        every { authTicketExchanger.exchange("ticket") } returns AuthTicketPayload(
            memberId = 1L,
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
        every { memberReader.readById(1L) } returns member

        val result = authService.exchangeTicket("ticket")

        result shouldBe AuthTicketExchangeResult(
            accessToken = accessToken,
            refreshToken = refreshToken,
            needsOnboarding = true,
        )
        verify(exactly = 1) { authTicketExchanger.exchange("ticket") }
        verify(exactly = 1) { memberReader.readById(1L) }
    }

    @Test
    fun `ticket 을 교환할 때 선호 팀이 없으면 온보딩 필요 여부를 함께 반환한다`() {
        val accessToken = "access-token"
        val refreshToken = "refresh-token"
        val member = Member(
            id = 1L,
            nickname = "tester",
            username = "tester-username",
            provider = MemberProvider.GOOGLE,
            providerKey = "provider-key",
            profileImage = "",
            description = "",
            preferredTeamId = null,
            role = MemberRole.USER,
        )
        every { authTicketExchanger.exchange("ticket") } returns AuthTicketPayload(
            memberId = 1L,
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
        every { memberReader.readById(1L) } returns member

        val result = authService.exchangeTicket("ticket")

        result shouldBe AuthTicketExchangeResult(
            accessToken = accessToken,
            refreshToken = refreshToken,
            needsOnboarding = true,
        )
        verify(exactly = 1) { authTicketExchanger.exchange("ticket") }
        verify(exactly = 1) { memberReader.readById(1L) }
    }

    @Test
    fun `ticket 을 교환할 때 닉네임과 선호 팀이 모두 있으면 온보딩이 필요하지 않다`() {
        val accessToken = "access-token"
        val refreshToken = "refresh-token"
        val member = Member(
            id = 1L,
            nickname = "tester",
            username = "tester-username",
            provider = MemberProvider.GOOGLE,
            providerKey = "provider-key",
            profileImage = "",
            description = "",
            preferredTeamId = 9L,
            role = MemberRole.USER,
        )
        every { authTicketExchanger.exchange("ticket") } returns AuthTicketPayload(
            memberId = 1L,
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
        every { memberReader.readById(1L) } returns member

        val result = authService.exchangeTicket("ticket")

        result shouldBe AuthTicketExchangeResult(
            accessToken = accessToken,
            refreshToken = refreshToken,
            needsOnboarding = false,
        )
        verify(exactly = 1) { authTicketExchanger.exchange("ticket") }
        verify(exactly = 1) { memberReader.readById(1L) }
    }

    @Test
    fun `refresh token 으로 access token 과 refresh token 을 재발급한다`() {
        val refreshToken = "refresh-token"
        val member = Member(
            id = 1L,
            nickname = "tester",
            username = "tester-username",
            provider = MemberProvider.GOOGLE,
            providerKey = "provider-key",
            profileImage = "",
            description = "",
            preferredTeamId = 9L,
            role = MemberRole.USER,
        )
        every { refreshTokenVerifier.verify(refreshToken) } returns AuthPrincipal(
            memberId = 1L,
            type = TokenType.REFRESH,
        )
        every { memberReader.readById(1L) } returns member
        every { authTokenIssuer.issue(1L, MemberRole.USER.name) } returns AuthTokens(
            accessToken = "new-access-token",
            refreshToken = "new-refresh-token",
        )
        every {
            refreshTokenWriter.write(1L, "new-refresh-token")
        } just runs

        val result = authService.reissue(refreshToken)

        result shouldBe AuthTokenResult(
            accessToken = "new-access-token",
            refreshToken = "new-refresh-token",
        )
        verify(exactly = 1) { refreshTokenVerifier.verify(refreshToken) }
        verify(exactly = 1) { memberReader.readById(1L) }
        verify(exactly = 1) { authTokenIssuer.issue(1L, MemberRole.USER.name) }
        verify(exactly = 1) { refreshTokenWriter.write(1L, "new-refresh-token") }
    }

    @Test
    fun `refresh token 으로 로그아웃한다`() {
        every { refreshTokenVerifier.verify("refresh-token") } returns AuthPrincipal(
            memberId = 1L,
            type = TokenType.REFRESH,
        )
        every { refreshTokenRemover.remove(1L) } just runs

        authService.logout("refresh-token")

        verify(exactly = 1) { refreshTokenVerifier.verify("refresh-token") }
        verify(exactly = 1) { refreshTokenRemover.remove(1L) }
    }

    @Test
    fun `oauth 회원이 처음 생성될 때 profile image 는 기본 이미지로 저장한다`() {
        every { memberReader.readByProvider(MemberProvider.GOOGLE, "provider-key") } returns null
        every { memberWriter.write(any()) } answers { firstArg() }

        authService.upsert(
            registrationId = "google",
            attributes = mapOf(
                "sub" to "provider-key",
            ),
        )

        verify(exactly = 1) {
            memberWriter.write(withArg { member ->
                member.profileImage shouldBe Member.DEFAULT_PROFILE_IMAGE_URL
            })
        }
    }

    @Test
    fun `oauth 회원이 처음 생성될 때 username 은 uuid 형식으로 저장한다`() {
        every { memberReader.readByProvider(MemberProvider.GOOGLE, "provider-key") } returns null
        every { memberWriter.write(any()) } answers { firstArg() }

        authService.upsert(
            registrationId = "google",
            attributes = mapOf(
                "sub" to "provider-key",
            ),
        )

        verify(exactly = 1) {
            memberWriter.write(withArg { member ->
                member.username.matches(
                    Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
                ) shouldBe true
            })
        }
    }
}
