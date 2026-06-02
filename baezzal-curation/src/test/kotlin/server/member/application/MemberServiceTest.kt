package server.member.application

import global.error.BadRequestException
import global.error.NotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.member.domain.Member
import server.member.domain.MemberProvider
import server.member.domain.MemberRole
import server.member.implementation.MemberNicknameGenerator
import server.member.implementation.MemberProfileImageUploader
import server.member.implementation.MemberReader
import server.objectstorage.PresignedUploadUrl
import server.team.domain.Team
import server.team.implementation.TeamReader

class MemberServiceTest {
    private val memberReader = mockk<MemberReader>()
    private val teamReader = mockk<TeamReader>()
    private val memberNicknameGenerator = mockk<MemberNicknameGenerator>()
    private val memberProfileImageUploader = mockk<MemberProfileImageUploader>()
    private val memberService = MemberService(
        memberReader = memberReader,
        teamReader = teamReader,
        memberNicknameGenerator = memberNicknameGenerator,
        memberProfileImageUploader = memberProfileImageUploader,
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

        val result = memberService.updateProfileImage(
            memberId = 1L,
            command = MemberProfileImageUpdateCommand(profileImage = "https://example.com/thumbnail.png"),
        )

        result shouldBe MemberData(
            nickname = "tester",
            preferredTeamId = 3L,
            profileImage = "https://example.com/thumbnail.png",
        )
        member.profileImage shouldBe "https://example.com/thumbnail.png"
        verify(exactly = 0) { memberNicknameGenerator.generateRandomNickname(any()) }
        verify(exactly = 0) { teamReader.readById(any()) }
    }

    @Test
    fun `profile image presigned url 을 발급한다`() {
        val fileName = slot<String>()
        val issued = PresignedUploadUrl(
            objectKey = "profiles/123e4567-e89b-12d3-a456-426614174000",
            uploadUrl = "https://s3.wowan.me/put",
            fileUrl = "https://static.wowan.me/file",
            headers = mapOf("Content-Type" to "image/png"),
            expiresInSeconds = 600,
        )
        every {
            memberProfileImageUploader.createPresignedUploadUrl(
                prefix = "profiles",
                fileName = capture(fileName),
                contentType = "image/png",
            )
        } returns issued

        val result = memberService.createProfileImageUploadUrl(
            memberId = 1L,
            command = CreateMemberProfileImageUploadUrlCommand(
                contentType = " IMAGE/PNG ",
            ),
        )

        result shouldBe MemberProfileImageUploadUrlResult.from(issued)
        UUID_REGEX.matches(fileName.captured) shouldBe true
    }

    @Test
    fun `profile image presigned url 발급 시 이미지가 아니면 예외가 발생한다`() {
        shouldThrow<BadRequestException> {
            memberService.createProfileImageUploadUrl(
                memberId = 1L,
                command = CreateMemberProfileImageUploadUrlCommand(
                    contentType = "application/pdf",
                ),
            )
        }

        verify(exactly = 0) {
            memberProfileImageUploader.createPresignedUploadUrl(any(), any(), any())
        }
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
        profileImage = "",
        preferredTeamId = preferredTeamId,
        role = MemberRole.USER,
    )

    companion object {
        private val UUID_REGEX =
            Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
    }
}
