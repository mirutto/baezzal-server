package server.member.application

import global.error.NotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.member.domain.Member
import server.member.domain.MemberProvider
import server.member.domain.MemberRole
import server.member.implementation.MemberCacheRemover
import server.member.implementation.MemberNicknameGenerator
import server.member.implementation.MemberProfileImageValidator
import server.member.implementation.MemberReader
import server.member.implementation.MemberEventPublisher
import server.member.implementation.MemberUsernameGenerator
import server.team.domain.Team
import server.team.domain.TeamCodes
import server.team.implementation.TeamReader

class MemberServiceTest {
    private val memberReader = mockk<MemberReader>()
    private val teamReader = mockk<TeamReader>()
    private val memberNicknameGenerator = mockk<MemberNicknameGenerator>()
    private val memberUsernameGenerator = mockk<MemberUsernameGenerator>()
    private val memberProfileImageValidator = mockk<MemberProfileImageValidator>()
    private val memberEventPublisher = mockk<MemberEventPublisher>()
    private val memberCacheRemover = mockk<MemberCacheRemover>()
    private val memberService = MemberService(
        memberReader = memberReader,
        teamReader = teamReader,
        memberNicknameGenerator = memberNicknameGenerator,
        memberUsernameGenerator = memberUsernameGenerator,
        memberProfileImageValidator = memberProfileImageValidator,
        memberEventPublisher = memberEventPublisher,
        memberCacheRemover = memberCacheRemover,
    )

    @Test
    fun `username 으로 회원 정보를 조회한다`() {
        val member = member(
            nickname = "tester",
            preferredTeamId = 3L,
            profileImage = "https://example.com/profile.png",
        )
        every { memberReader.readByUsername("tester-username") } returns member
        every { teamReader.resolveCode(3L) } returns TeamCodes.SSG

        val result = memberService.findByUsername("tester-username")

        result shouldBe MemberResult(
            nickname = "tester",
            username = "tester-username",
            description = "tester-description",
            preferredTeamCode = TeamCodes.SSG,
            profileImage = "https://example.com/profile.png",
            needsOnboarding = false,
        )
    }

    @Test
    fun `온보딩에서 생성된 nickname 과 preferred team 을 함께 수정한다`() {
        val member = member(
            nickname = "before",
            preferredTeamId = null,
        )
        every { memberReader.readById(1L) } returns member
        every { teamReader.readByCode(TeamCodes.HANWHA) } returns Team(
            id = 2L,
            code = TeamCodes.HANWHA,
            name = "LG",
            sortOrder = 1,
        )
        every { memberNicknameGenerator.generateRandomNickname(TeamCodes.HANWHA) } returns "홈런왕 독수리"
        every { memberUsernameGenerator.generateRandomUsername(TeamCodes.HANWHA) } returns "hanwha-1234abcd"
        every { memberEventPublisher.publishUpdated(member) } returns Unit

        val result = memberService.onboarding(
            memberId = 1L,
            command = MemberOnboardingCommand(
                preferredTeamCode = TeamCodes.HANWHA,
            ),
        )

        result shouldBe MemberData(
            nickname = "홈런왕 독수리",
            username = "hanwha-1234abcd",
            description = "before-description",
            preferredTeamCode = TeamCodes.HANWHA,
            profileImage = "",
        )
        member.nickname shouldBe "홈런왕 독수리"
        member.username shouldBe "hanwha-1234abcd"
        member.preferredTeamId shouldBe 2L
        verify(exactly = 1) { memberEventPublisher.publishUpdated(member) }
        verify(exactly = 1) { memberNicknameGenerator.generateRandomNickname(TeamCodes.HANWHA) }
        verify(exactly = 1) { memberUsernameGenerator.generateRandomUsername(TeamCodes.HANWHA) }
    }

    @Test
    fun `nickname 만 수정한다`() {
        val member = member(
            nickname = "before",
            preferredTeamId = 3L,
        )
        every { memberReader.readById(1L) } returns member
        every { teamReader.resolveCode(3L) } returns TeamCodes.SSG
        every { memberEventPublisher.publishUpdated(member) } returns Unit

        val result = memberService.updateNickname(
            memberId = 1L,
            command = MemberNicknameUpdateCommand(nickname = "after"),
        )

        result shouldBe MemberData(
            nickname = "after",
            username = "before-username",
            description = "before-description",
            preferredTeamCode = TeamCodes.SSG,
            profileImage = "",
        )
        member.nickname shouldBe "after"
        verify(exactly = 1) { memberEventPublisher.publishUpdated(member) }
        verify(exactly = 0) { memberNicknameGenerator.generateRandomNickname(any()) }
        verify(exactly = 0) { memberUsernameGenerator.generateRandomUsername(any()) }
    }

    @Test
    fun `preferred team 을 null 로 수정할 수 있다`() {
        val member = member(
            nickname = "tester",
            preferredTeamId = 2L,
        )
        every { memberReader.readById(1L) } returns member
        every { memberEventPublisher.publishUpdated(member) } returns Unit

        val result = memberService.updatePreferredTeam(
            memberId = 1L,
            command = MemberPreferredTeamUpdateCommand(preferredTeamCode = null),
        )

        result shouldBe MemberData(
            nickname = "tester",
            username = "tester-username",
            description = "tester-description",
            preferredTeamCode = null,
            profileImage = "",
        )
        member.preferredTeamId shouldBe null
        verify(exactly = 1) { memberEventPublisher.publishUpdated(member) }
        verify(exactly = 0) { memberNicknameGenerator.generateRandomNickname(any()) }
        verify(exactly = 0) { memberUsernameGenerator.generateRandomUsername(any()) }
    }

    @Test
    fun `profile image 만 수정한다`() {
        val member = member(
            nickname = "tester",
            preferredTeamId = 3L,
        )
        every { memberReader.readById(1L) } returns member
        every { teamReader.resolveCode(3L) } returns TeamCodes.SSG
        every { memberProfileImageValidator.validateImageUrl("https://example.com/thumbnail.png") } returns Unit
        every { memberEventPublisher.publishUpdated(member) } returns Unit

        val result = memberService.updateProfileImage(
            memberId = 1L,
            command = MemberProfileImageUpdateCommand(profileImage = "https://example.com/thumbnail.png"),
        )

        result shouldBe MemberData(
            nickname = "tester",
            username = "tester-username",
            description = "tester-description",
            preferredTeamCode = TeamCodes.SSG,
            profileImage = "https://example.com/thumbnail.png",
        )
        verify(exactly = 1) {
            memberProfileImageValidator.validateImageUrl("https://example.com/thumbnail.png")
        }
        member.profileImage shouldBe "https://example.com/thumbnail.png"
        verify(exactly = 1) { memberEventPublisher.publishUpdated(member) }
        verify(exactly = 0) { memberNicknameGenerator.generateRandomNickname(any()) }
        verify(exactly = 0) { memberUsernameGenerator.generateRandomUsername(any()) }
    }

    @Test
    fun `존재하지 않는 preferred team 으로 수정하면 예외가 발생한다`() {
        val member = member(
            nickname = "tester",
            preferredTeamId = null,
        )
        every { memberReader.readById(1L) } returns member
        every { teamReader.readByCode("MISSING") } throws NotFoundException("팀을 찾을 수 없습니다")

        shouldThrow<NotFoundException> {
            memberService.updatePreferredTeam(
                memberId = 1L,
                command = MemberPreferredTeamUpdateCommand(preferredTeamCode = "MISSING"),
            )
        }
    }

    @Test
    fun `member updated event 로 member 캐시를 무효화한다`() {
        every { memberCacheRemover.remove(1L, "tester-username") } returns Unit

        memberService.handleUpdated(
            MemberUpdatedEvent(
                memberId = 1L,
                username = "tester-username",
            ),
        )

        verify(exactly = 1) { memberCacheRemover.remove(1L, "tester-username") }
    }

    private fun member(
        nickname: String,
        preferredTeamId: Long?,
        profileImage: String = "",
        username: String = "$nickname-username",
        description: String = "$nickname-description",
    ): Member = Member(
        id = 1L,
        nickname = nickname,
        username = username,
        provider = MemberProvider.GOOGLE,
        providerKey = "provider-key",
        profileImage = profileImage,
        description = description,
        preferredTeamId = preferredTeamId,
        role = MemberRole.USER,
    )

}
