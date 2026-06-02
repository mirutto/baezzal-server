package server.member.application

import global.error.BadRequestException
import global.error.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import server.member.domain.Member
import server.member.implementation.MemberNicknameGenerator
import server.member.implementation.MemberProfileImageUploader
import server.member.implementation.MemberReader
import server.team.implementation.TeamReader
import java.util.UUID

@Service
class MemberService(
    private val memberReader: MemberReader,
    private val teamReader: TeamReader,
    private val memberNicknameGenerator: MemberNicknameGenerator,
    private val memberProfileImageUploader: MemberProfileImageUploader,
) {
    @Transactional
    fun onboarding(
        memberId: Long,
        command: MemberOnboardingCommand,
    ): MemberData {
        val member = readMember(memberId)
        val preferredTeamId = validatePreferredTeamId(command.preferredTeamId)

        val randomNickname =
            memberNicknameGenerator.generateRandomNickname(command.preferredTeamId)
        member.updateNickname(randomNickname)
        member.updatePreferredTeam(preferredTeamId)

        return MemberData(member)
    }

    @Transactional
    fun updateNickname(
        memberId: Long,
        command: MemberNicknameUpdateCommand,
    ): MemberData {
        val member = readMember(memberId)
        member.updateNickname(command.nickname)

        return MemberData(member)
    }

    @Transactional
    fun updatePreferredTeam(
        memberId: Long,
        command: MemberPreferredTeamUpdateCommand,
    ): MemberData {
        val member = readMember(memberId)
        val preferredTeamId = validatePreferredTeamId(command.preferredTeamId)

        member.updatePreferredTeam(preferredTeamId)

        return MemberData(member)
    }

    @Transactional
    fun updateProfileImage(
        memberId: Long,
        command: MemberProfileImageUpdateCommand,
    ): MemberData {
        val member = readMember(memberId)
        member.updateProfileImage(command.profileImage)

        return MemberData(member)
    }

    fun createProfileImageUploadUrl(
        memberId: Long,
        command: CreateMemberProfileImageUploadUrlCommand,
    ): MemberProfileImageUploadUrlResult {
        require(memberId > 0)
        val contentType = command.contentType.trim().lowercase()
        validateImageContentType(contentType)

        val uploadUrl =
            memberProfileImageUploader.createPresignedUploadUrl(
                prefix = PROFILE_IMAGE_PREFIX,
                fileName = UUID.randomUUID().toString(),
                contentType = contentType,
            )

        return MemberProfileImageUploadUrlResult.from(uploadUrl)
    }

    private fun readMember(memberId: Long): Member =
        memberReader.readById(memberId)
            ?: throw NotFoundException("회원을 찾을 수 없습니다")

    private fun validatePreferredTeamId(preferredTeamId: Long?): Long? {
        if (preferredTeamId == null) {
            return null
        }

        teamReader.readById(preferredTeamId)
            ?: throw NotFoundException("팀을 찾을 수 없습니다")

        return preferredTeamId
    }

    private fun validateImageContentType(contentType: String) {
        if (!contentType.startsWith(IMAGE_CONTENT_TYPE_PREFIX)) {
            throw BadRequestException("이미지 파일만 업로드할 수 있습니다")
        }
    }

    companion object {
        private const val PROFILE_IMAGE_PREFIX = "profiles"
        private const val IMAGE_CONTENT_TYPE_PREFIX = "image/"
    }
}
