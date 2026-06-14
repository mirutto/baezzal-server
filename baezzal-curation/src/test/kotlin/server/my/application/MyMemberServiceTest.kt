package server.my.application

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import server.member.domain.Member
import server.member.domain.MemberProvider
import server.member.domain.MemberRole
import server.member.implementation.MemberCachedReader
import server.team.domain.TeamCodes
import server.team.implementation.TeamReader

class MyMemberServiceTest {
    private val memberCachedReader = mockk<MemberCachedReader>()
    private val teamReader = mockk<TeamReader>()
    private val myMemberService = MyMemberService(
        memberCachedReader = memberCachedReader,
        teamReader = teamReader,
    )

    @Test
    fun `내 정보를 조회한다`() {
        val member = member(
            nickname = "tester",
            preferredTeamId = 3L,
            profileImage = "https://example.com/profile.png",
        )
        every { memberCachedReader.readById(1L) } returns member
        every { teamReader.resolveCode(3L) } returns TeamCodes.SSG

        val result = myMemberService.getMyProfile(1L)

        result shouldBe MyMemberResult(
            nickname = "tester",
            username = "tester-username",
            description = "tester-description",
            profileImage = "https://example.com/profile.png",
            preferredTeamCode = TeamCodes.SSG,
            needsOnboarding = false,
        )
    }

    @Test
    fun `선호 팀이 없으면 온보딩이 필요하다`() {
        val member = member(
            nickname = "tester",
            preferredTeamId = null,
        )
        every { memberCachedReader.readById(1L) } returns member
        every { teamReader.resolveCode(null) } returns null

        val result = myMemberService.getMyProfile(1L)

        result shouldBe MyMemberResult(
            nickname = "tester",
            username = "tester-username",
            description = "tester-description",
            profileImage = "",
            preferredTeamCode = null,
            needsOnboarding = true,
        )
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
