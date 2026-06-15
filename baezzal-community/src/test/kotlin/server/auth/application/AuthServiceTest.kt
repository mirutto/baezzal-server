package server.auth.application
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
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
import server.member.implementation.MemberEventPublisher
import server.member.implementation.MemberReader
import server.member.implementation.MemberWriter
import server.token.AuthPrincipal
import server.token.TokenType

class AuthServiceTest {
    private val memberReader = mockk<MemberReader>()
    private val memberWriter = mockk<MemberWriter>()
    private val memberEventPublisher = mockk<MemberEventPublisher>()
    private val authTicketIssuer = mockk<AuthTicketIssuer>()
    private val authTicketExchanger = mockk<AuthTicketExchanger>()
    private val authTokenIssuer = mockk<AuthTokenIssuer>()
    private val refreshTokenVerifier = mockk<RefreshTokenVerifier>()
    private val refreshTokenWriter = mockk<RefreshTokenWriter>()
    private val refreshTokenRemover = mockk<RefreshTokenRemover>()
    private val authService = AuthService(
        memberReader = memberReader,
        memberWriter = memberWriter,
        memberEventPublisher = memberEventPublisher,
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
            profileImage = Member.defaultProfileImage(),
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
            profileImage = Member.defaultProfileImage(),
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
            profileImage = Member.defaultProfileImage(),
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
        val sessionId = "session-id"
        val member = Member(
            id = 1L,
            nickname = "tester",
            username = "tester-username",
            provider = MemberProvider.GOOGLE,
            providerKey = "provider-key",
            profileImage = Member.defaultProfileImage(),
            description = "",
            preferredTeamId = 9L,
            role = MemberRole.USER,
        )
        every { refreshTokenVerifier.verify(refreshToken) } returns AuthPrincipal(
            memberId = 1L,
            type = TokenType.REFRESH,
            sessionId = sessionId,
        )
        every { memberReader.readById(1L) } returns member
        every { authTokenIssuer.issue(1L, MemberRole.USER.name, sessionId) } returns AuthTokens(
            accessToken = "new-access-token",
            refreshToken = "new-refresh-token",
        )
        every {
            refreshTokenWriter.write(sessionId, "new-refresh-token")
        } just runs

        val result = authService.reissue(refreshToken)

        result shouldBe AuthTokenResult(
            accessToken = "new-access-token",
            refreshToken = "new-refresh-token",
        )
        verify(exactly = 1) { refreshTokenVerifier.verify(refreshToken) }
        verify(exactly = 1) { memberReader.readById(1L) }
        verify(exactly = 1) { authTokenIssuer.issue(1L, MemberRole.USER.name, sessionId) }
        verify(exactly = 1) { refreshTokenWriter.write(sessionId, "new-refresh-token") }
    }

    @Test
    fun `refresh token 으로 로그아웃한다`() {
        every { refreshTokenVerifier.verify("refresh-token") } returns AuthPrincipal(
            memberId = 1L,
            type = TokenType.REFRESH,
            sessionId = "session-id",
        )
        every { refreshTokenRemover.remove("session-id") } just runs

        authService.logout("refresh-token")

        verify(exactly = 1) { refreshTokenVerifier.verify("refresh-token") }
        verify(exactly = 1) { refreshTokenRemover.remove("session-id") }
    }

    @Test
    fun `로그인 ticket 발급 시 새 session id 로 refresh token 을 저장한다`() {
        val capturedSessionId = slot<String>()
        every { authTokenIssuer.issue(1L, "USER", capture(capturedSessionId)) } returns AuthTokens(
            accessToken = "access-token",
            refreshToken = "refresh-token",
        )
        every { refreshTokenWriter.write(any(), "refresh-token") } just runs
        every {
            authTicketIssuer.issue(
                memberId = 1L,
                accessToken = "access-token",
                refreshToken = "refresh-token",
            )
        } returns "ticket"

        val result = authService.issueLoginTicket(1L, "USER")

        result shouldBe "ticket"
        capturedSessionId.captured.isBlank() shouldBe false
        verify(exactly = 1) { authTokenIssuer.issue(1L, "USER", capturedSessionId.captured) }
        verify(exactly = 1) { refreshTokenWriter.write(capturedSessionId.captured, "refresh-token") }
    }

    @Test
    fun `oauth 회원이 처음 생성될 때 profile image 는 기본 이미지로 저장한다`() {
        every { memberReader.readByProvider(MemberProvider.GOOGLE, "provider-key") } returns null
        every { memberWriter.write(any()) } answers { firstArg() }
        every { memberEventPublisher.publishCreated(any()) } just runs

        authService.upsert(
            registrationId = "google",
            attributes = mapOf(
                "sub" to "provider-key",
            ),
        )

        verify(exactly = 1) {
            memberWriter.write(withArg { member ->
                member.profileImage.rawUrl shouldBe Member.DEFAULT_PROFILE_IMAGE_URL
                member.profileImage.publicUrl shouldBe Member.DEFAULT_PROFILE_PUBLIC_URL
                member.profileImage.thumbnailUrl shouldBe Member.DEFAULT_PROFILE_THUMBNAIL_URL
            })
        }
    }

    @Test
    fun `oauth 회원이 처음 생성될 때 username 은 uuid 형식으로 저장한다`() {
        every { memberReader.readByProvider(MemberProvider.GOOGLE, "provider-key") } returns null
        every { memberWriter.write(any()) } answers { firstArg<Member>() }
        every { memberEventPublisher.publishCreated(any()) } just runs

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

    @Test
    fun `oauth 회원이 처음 생성될 때 member created event 를 발행한다`() {
        every { memberReader.readByProvider(MemberProvider.GOOGLE, "provider-key") } returns null
        every { memberWriter.write(any()) } answers {
            val saved = firstArg<Member>()
            Member(
                id = 3L,
                nickname = saved.nickname,
                username = saved.username,
                provider = saved.provider,
                providerKey = saved.providerKey,
                profileImage = saved.profileImage,
                description = saved.description,
                preferredTeamId = saved.preferredTeamId,
                role = saved.role,
            )
        }
        every { memberEventPublisher.publishCreated(3L) } just runs

        val result = authService.upsert(
            registrationId = "google",
            attributes = mapOf(
                "sub" to "provider-key",
            ),
        )

        result shouldBe Oauth2LoginResult(
            memberId = 3L,
            role = MemberRole.USER.name,
        )
        verify(exactly = 1) { memberEventPublisher.publishCreated(3L) }
    }

    @Test
    fun `이미 존재하는 oauth 회원이면 member created event 를 발행하지 않는다`() {
        every { memberReader.readByProvider(MemberProvider.GOOGLE, "provider-key") } returns Member(
            id = 5L,
            nickname = "tester",
            username = "tester-username",
            provider = MemberProvider.GOOGLE,
            providerKey = "provider-key",
            profileImage = Member.defaultProfileImage(),
            description = "",
            preferredTeamId = 1L,
            role = MemberRole.USER,
        )

        val result = authService.upsert(
            registrationId = "google",
            attributes = mapOf(
                "sub" to "provider-key",
            ),
        )

        result shouldBe Oauth2LoginResult(
            memberId = 5L,
            role = MemberRole.USER.name,
        )
        verify(exactly = 0) { memberWriter.write(any()) }
        verify(exactly = 0) { memberEventPublisher.publishCreated(any()) }
    }
}
