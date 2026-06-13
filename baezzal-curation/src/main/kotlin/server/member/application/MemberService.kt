package server.member.application

import global.error.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import server.member.implementation.MemberNicknameGenerator
import server.member.implementation.MemberProfileImageValidator
import server.member.implementation.MemberCacheRemover
import server.member.implementation.MemberReader
import server.member.implementation.MemberEventPublisher
import server.member.implementation.MemberUsernameGenerator
import server.team.implementation.TeamReader

@Service
class MemberService(
    private val memberReader: MemberReader,
    private val teamReader: TeamReader,
    private val memberNicknameGenerator: MemberNicknameGenerator,
    private val memberUsernameGenerator: MemberUsernameGenerator,
    private val memberProfileImageValidator: MemberProfileImageValidator,
    private val memberEventPublisher: MemberEventPublisher,
    private val memberCacheRemover: MemberCacheRemover,
) {
    @Transactional(readOnly = true)
    fun findByUsername(username: String): MemberResult =
        MemberResult(memberReader.readByUsername(username))

    @Transactional
    fun onboarding(
        memberId: Long,
        command: MemberOnboardingCommand,
    ): MemberData {
        val member = memberReader.readById(memberId)
        val preferredTeamId = validatePreferredTeamId(command.preferredTeamId)

        val randomNickname =
            memberNicknameGenerator.generateRandomNickname(command.preferredTeamId)
        val randomUsername =
            memberUsernameGenerator.generateRandomUsername(command.preferredTeamId)
        member.updateNickname(randomNickname)
        member.updatePreferredTeam(preferredTeamId)
        member.updateUsername(randomUsername)
        memberEventPublisher.publishUpdated(member)

        return MemberData(member)
    }

    @Transactional
    fun updateNickname(
        memberId: Long,
        command: MemberNicknameUpdateCommand,
    ): MemberData {
        val member = memberReader.readById(memberId)
        member.updateNickname(command.nickname)
        memberEventPublisher.publishUpdated(member)

        return MemberData(member)
    }

    @Transactional
    fun updatePreferredTeam(
        memberId: Long,
        command: MemberPreferredTeamUpdateCommand,
    ): MemberData {
        val member = memberReader.readById(memberId)
        val preferredTeamId = validatePreferredTeamId(command.preferredTeamId)

        member.updatePreferredTeam(preferredTeamId)
        memberEventPublisher.publishUpdated(member)

        return MemberData(member)
    }

    @Transactional
    fun updateProfileImage(
        memberId: Long,
        command: MemberProfileImageUpdateCommand,
    ): MemberData {
        val profileImage = command.profileImage.trim()
        val member = memberReader.readById(memberId)
        memberProfileImageValidator.validateImageUrl(profileImage)
        member.updateProfileImage(profileImage)
        memberEventPublisher.publishUpdated(member)

        return MemberData(member)
    }

    @Transactional
    fun handleUpdated(event: MemberUpdatedEvent) {
        memberCacheRemover.remove(
            memberId = event.memberId,
            username = event.username,
        )
    }

    private fun validatePreferredTeamId(preferredTeamId: Long?): Long? {
        if (preferredTeamId == null) {
            return null
        }

        teamReader.readById(preferredTeamId)
            ?: throw NotFoundException("팀을 찾을 수 없습니다")

        return preferredTeamId
    }
}
