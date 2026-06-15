package server.my.application

import global.image.ImageStatus
import global.image.ImageVersions
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import server.follow.implementation.FollowReader
import server.member.application.MemberData
import server.member.domain.Member
import server.member.domain.MemberProvider
import server.member.domain.MemberRole
import server.team.implementation.TeamReader

class MyFollowServiceTest {
    private val followReader = mockk<FollowReader>()
    private val teamReader = mockk<TeamReader>()
    private val myFollowService = MyFollowService(
        followReader = followReader,
        teamReader = teamReader,
    )

    @Test
    fun `내 팔로우 통계를 조회한다`() {
        every { followReader.readFollowerCount(1L) } returns 3L
        every { followReader.readFollowingCount(1L) } returns 7L

        val result = myFollowService.getMyStats(1L)

        result shouldBe MyFollowStats(
            followerCount = 3L,
            followeeCount = 7L,
        )
    }

    @Test
    fun `내 팔로워 목록을 조회한다`() {
        every { followReader.readFollowerMembers(1L) } returns listOf(
            member(id = 2L, nickname = "member-2"),
            member(id = 3L, nickname = "member-3"),
        )
        every { teamReader.resolveCode(null) } returns null

        val result = myFollowService.getMyFollowers(1L)

        result shouldBe listOf(
            MemberData(
                nickname = "member-2",
                username = "member-2-username",
                description = "member-2-description",
                preferredTeamCode = null,
                publicProfileImageUrl = Member.DEFAULT_PROFILE_PUBLIC_URL,
                thumbnailProfileImageUrl = Member.DEFAULT_PROFILE_THUMBNAIL_URL,
            ),
            MemberData(
                nickname = "member-3",
                username = "member-3-username",
                description = "member-3-description",
                preferredTeamCode = null,
                publicProfileImageUrl = Member.DEFAULT_PROFILE_PUBLIC_URL,
                thumbnailProfileImageUrl = Member.DEFAULT_PROFILE_THUMBNAIL_URL,
            ),
        )
    }

    @Test
    fun `내 팔로잉 목록을 조회한다`() {
        every { followReader.readFollowingMembers(1L) } returns listOf(
            member(id = 4L, nickname = "member-4"),
        )
        every { teamReader.resolveCode(null) } returns null

        val result = myFollowService.getMyFollowings(1L)

        result shouldBe listOf(
            MemberData(
                nickname = "member-4",
                username = "member-4-username",
                description = "member-4-description",
                preferredTeamCode = null,
                publicProfileImageUrl = Member.DEFAULT_PROFILE_PUBLIC_URL,
                thumbnailProfileImageUrl = Member.DEFAULT_PROFILE_THUMBNAIL_URL,
            ),
        )
    }

    private fun member(
        id: Long,
        nickname: String,
    ): Member = Member(
        id = id,
        nickname = nickname,
        username = "$nickname-username",
        provider = MemberProvider.GOOGLE,
        providerKey = "provider-key-$id",
        profileImage = ImageVersions(
            rawUrl = Member.DEFAULT_PROFILE_IMAGE_URL,
            publicUrl = Member.DEFAULT_PROFILE_PUBLIC_URL,
            thumbnailUrl = Member.DEFAULT_PROFILE_THUMBNAIL_URL,
            status = ImageStatus.SUCCESS,
            aspectRatio = 1.0,
        ),
        description = "$nickname-description",
        preferredTeamId = null,
        role = MemberRole.USER,
    )
}
