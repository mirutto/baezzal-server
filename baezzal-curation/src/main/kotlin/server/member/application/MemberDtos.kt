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
    val username: String,
    val description: String,
    val preferredTeamId: Long?,
    val profileImage: String,
) {
    constructor(member: Member) : this(
        nickname = member.nickname,
        username = member.username,
        description = member.description,
        preferredTeamId = member.preferredTeamId,
        profileImage = member.profileImage,
    )
}

data class MemberMeResult(
    val nickname: String,
    val username: String,
    val description: String,
    val profileImage: String,
    val preferredTeamId: Long?,
    val needsOnboarding: Boolean,
) {
    constructor(member: Member) : this(
        nickname = member.nickname,
        username = member.username,
        description = member.description,
        profileImage = member.profileImage,
        preferredTeamId = member.preferredTeamId,
        needsOnboarding = member.isNew(),
    )
}
