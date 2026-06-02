package server.member.application

import server.member.domain.Member

data class MemberOnboardingCommand(
    val preferredTeamId: Long,
)

data class MemberNicknameUpdateCommand(
    val nickname: String,
)

data class MemberPreferredTeamUpdateCommand(
    val preferredTeamId: Long?,
)

data class MemberProfileImageUpdateCommand(
    val profileImage: String,
)

data class MemberData(
    val nickname: String,
    val preferredTeamId: Long?,
    val profileImage: String,
) {
    constructor(member: Member) : this(
        nickname = member.nickname,
        preferredTeamId = member.preferredTeamId,
        profileImage = member.profileImage,
    )
}
