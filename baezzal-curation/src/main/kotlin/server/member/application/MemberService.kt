package server.member.application

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
        memberReader.readByUsername(username).let { member ->
            MemberResult(member, teamReader.resolveCode(member.preferredTeamId))
        }

    @Transactional
    fun onboarding(
        memberId: Long,
        command: MemberOnboardingCommand,
    ): MemberData {
        val member = memberReader.readById(memberId)
        val preferredTeam = teamReader.readByCode(command.preferredTeamCode)

        val randomNickname =
            memberNicknameGenerator.generateRandomNickname(command.preferredTeamCode)
        val randomUsername =
            memberUsernameGenerator.generateRandomUsername(command.preferredTeamCode)
        member.updateNickname(randomNickname)
        member.updatePreferredTeam(preferredTeam.id)
        member.updateUsername(randomUsername)
        memberEventPublisher.publishUpdated(member)

        return MemberData(member, preferredTeam.code)
    }

    @Transactional
    fun updateNickname(
        memberId: Long,
        command: MemberNicknameUpdateCommand,
    ): MemberData {
        val member = memberReader.readById(memberId)
        member.updateNickname(command.nickname)
        memberEventPublisher.publishUpdated(member)

        return MemberData(member, teamReader.resolveCode(member.preferredTeamId))
    }

    @Transactional
    fun updatePreferredTeam(
        memberId: Long,
        command: MemberPreferredTeamUpdateCommand,
    ): MemberData {
        val member = memberReader.readById(memberId)
        val preferredTeam = command.preferredTeamCode?.let(teamReader::readByCode)

        member.updatePreferredTeam(preferredTeam?.id)
        memberEventPublisher.publishUpdated(member)

        return MemberData(member, preferredTeam?.code)
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

        return MemberData(member, teamReader.resolveCode(member.preferredTeamId))
    }

    @Transactional
    fun handleUpdated(event: MemberUpdatedEvent) {
        memberCacheRemover.remove(
            memberId = event.memberId,
            username = event.username,
        )
    }
}
