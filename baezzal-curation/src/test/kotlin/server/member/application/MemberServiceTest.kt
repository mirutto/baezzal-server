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
import server.member.implementation.MemberNicknameGenerator
import server.member.implementation.MemberProfileImageValidator
import server.member.implementation.MemberReader
import server.team.domain.Team
import server.team.implementation.TeamReader

class MemberServiceTest {
    private val memberReader = mockk<MemberReader>()
    private val teamReader = mockk<TeamReader>()
    private val memberNicknameGenerator = mockk<MemberNicknameGenerator>()
    private val memberProfileImageValidator = mockk<MemberProfileImageValidator>()
    private val memberService = MemberService(
        memberReader = memberReader,
        teamReader = teamReader,
        memberNicknameGenerator = memberNicknameGenerator,
        memberProfileImageValidator = memberProfileImageValidator,
    )

    @Test
    fun `내 정보를 조회한다`() {
        val member = member(
            nickname = "tester",
            preferredTeamId = 3L,
            profileImage = "https://example.com/profile.png",
        )
        every { memberReader.readById(1L) } returns member

        val result = memberService.getMe(1L)

        result shouldBe MemberMeResult(
            nickname = "tester",
            profileImage = "https://example.com/profile.png",
            preferredTeamId = 3L,
            needsOnboarding = false,
        )
        verify(exactly = 0) { memberNicknameGenerator.generateRandomNickname(any()) }
        verify(exactly = 0) { teamReader.readById(any()) }
    }

    @Test
    fun `선호 팀이 없으면 온보딩이 필요하다`() {
        val member = member(
            nickname = "tester",
            preferredTeamId = null,
        )
        every { memberReader.readById(1L) } returns member

        val result = memberService.getMe(1L)

        result shouldBe MemberMeResult(
            nickname = "tester",
            profileImage = "",
            preferredTeamId = null,
            needsOnboarding = true,
        )
    }

    @Test
    fun `온보딩에서 생성된 nickname 과 preferred team 을 함께 수정한다`() {
        val member = member(
            nickname = "before",
            preferredTeamId = null,
        )
        every { memberReader.readById(1L) } returns member
        every { teamReader.readById(2L) } returns Team(
            id = 2L,
            name = "LG",
            sortOrder = 1,
        )
        every { memberNicknameGenerator.generateRandomNickname(2L) } returns "홈런왕 쌍둥이"

        val result = memberService.onboarding(
            memberId = 1L,
            command = MemberOnboardingCommand(
                preferredTeamId = 2L,
            ),
        )

        result shouldBe MemberData(
            nickname = "홈런왕 쌍둥이",
            preferredTeamId = 2L,
            profileImage = "",
        )
        member.nickname shouldBe "홈런왕 쌍둥이"
        member.preferredTeamId shouldBe 2L
        verify(exactly = 1) { memberNicknameGenerator.generateRandomNickname(2L) }
    }

    @Test
    fun `nickname 만 수정한다`() {
        val member = member(
            nickname = "before",
            preferredTeamId = 3L,
        )
        every { memberReader.readById(1L) } returns member

        val result = memberService.updateNickname(
            memberId = 1L,
            command = MemberNicknameUpdateCommand(nickname = "after"),
        )

        result shouldBe MemberData(
            nickname = "after",
            preferredTeamId = 3L,
            profileImage = "",
        )
        member.nickname shouldBe "after"
        verify(exactly = 0) { memberNicknameGenerator.generateRandomNickname(any()) }
        verify(exactly = 0) { teamReader.readById(any()) }
    }

    @Test
    fun `preferred team 을 null 로 수정할 수 있다`() {
        val member = member(
            nickname = "tester",
            preferredTeamId = 2L,
        )
        every { memberReader.readById(1L) } returns member

        val result = memberService.updatePreferredTeam(
            memberId = 1L,
            command = MemberPreferredTeamUpdateCommand(preferredTeamId = null),
        )

        result shouldBe MemberData(
            nickname = "tester",
            preferredTeamId = null,
            profileImage = "",
        )
        member.preferredTeamId shouldBe null
        verify(exactly = 0) { memberNicknameGenerator.generateRandomNickname(any()) }
        verify(exactly = 0) { teamReader.readById(any()) }
    }

    @Test
    fun `profile image 만 수정한다`() {
        val member = member(
            nickname = "tester",
            preferredTeamId = 3L,
        )
        every { memberReader.readById(1L) } returns member
        every { memberProfileImageValidator.validateImageUrl("https://example.com/thumbnail.png") } returns Unit

        val result = memberService.updateProfileImage(
            memberId = 1L,
            command = MemberProfileImageUpdateCommand(profileImage = "https://example.com/thumbnail.png"),
        )

        result shouldBe MemberData(
            nickname = "tester",
            preferredTeamId = 3L,
            profileImage = "https://example.com/thumbnail.png",
        )
        verify(exactly = 1) {
            memberProfileImageValidator.validateImageUrl("https://example.com/thumbnail.png")
        }
        member.profileImage shouldBe "https://example.com/thumbnail.png"
        verify(exactly = 0) { memberNicknameGenerator.generateRandomNickname(any()) }
        verify(exactly = 0) { teamReader.readById(any()) }
    }

    @Test
    fun `존재하지 않는 preferred team 으로 수정하면 예외가 발생한다`() {
        val member = member(
            nickname = "tester",
            preferredTeamId = null,
        )
        every { memberReader.readById(1L) } returns member
        every { teamReader.readById(9L) } returns null

        shouldThrow<NotFoundException> {
            memberService.updatePreferredTeam(
                memberId = 1L,
                command = MemberPreferredTeamUpdateCommand(preferredTeamId = 9L),
            )
        }
    }

    private fun member(
        nickname: String,
        preferredTeamId: Long?,
        profileImage: String = "",
    ): Member = Member(
        id = 1L,
        nickname = nickname,
        provider = MemberProvider.GOOGLE,
        providerKey = "provider-key",
        profileImage = profileImage,
        preferredTeamId = preferredTeamId,
        role = MemberRole.USER,
    )
}
