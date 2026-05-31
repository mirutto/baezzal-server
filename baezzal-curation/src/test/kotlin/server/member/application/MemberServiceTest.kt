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
import server.member.implementation.MemberReader
import server.team.domain.Team
import server.team.implementation.TeamReader

class MemberServiceTest {
    private val memberReader = mockk<MemberReader>()
    private val teamReader = mockk<TeamReader>()
    private val memberNicknameGenerator = mockk<MemberNicknameGenerator>()
    private val memberService = MemberService(
        memberReader = memberReader,
        teamReader = teamReader,
        memberNicknameGenerator = memberNicknameGenerator,
    )

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
        )
        member.preferredTeamId shouldBe null
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
    ): Member = Member(
        id = 1L,
        nickname = nickname,
        provider = MemberProvider.GOOGLE,
        providerKey = "provider-key",
        preferredTeamId = preferredTeamId,
        role = MemberRole.USER,
    )
}
