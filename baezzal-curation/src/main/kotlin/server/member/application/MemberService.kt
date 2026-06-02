package server.member.application

import global.error.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import server.member.domain.Member
import server.member.implementation.MemberNicknameGenerator
import server.member.implementation.MemberProfileImageValidator
import server.member.implementation.MemberReader
import server.team.implementation.TeamReader

@Service
class MemberService(
    private val memberReader: MemberReader,
    private val teamReader: TeamReader,
    private val memberNicknameGenerator: MemberNicknameGenerator,
    private val memberProfileImageValidator: MemberProfileImageValidator,
) {
    @Transactional(readOnly = true)
    fun getMe(memberId: Long): MemberMeResult {
        val member = readMember(memberId)
        return MemberMeResult(member)
    }

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
        val profileImage = command.profileImage.trim()
        val member = readMember(memberId)
        memberProfileImageValidator.validateImageUrl(profileImage)
        member.updateProfileImage(profileImage)

        return MemberData(member)
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
}
