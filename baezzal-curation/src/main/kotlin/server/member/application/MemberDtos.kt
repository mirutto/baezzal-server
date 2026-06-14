package server.member.application

import server.member.domain.Member

data class MemberOnboardingCommand(
    val preferredTeamCode: String,
)

data class MemberNicknameUpdateCommand(
    val nickname: String,
)

data class MemberPreferredTeamUpdateCommand(
    val preferredTeamCode: String?,
)

data class MemberProfileImageUpdateCommand(
    val profileImage: String,
)

data class MemberData(
    val nickname: String,
    val username: String,
    val description: String,
    val preferredTeamCode: String?,
    val profileImage: String,
) {
    constructor(member: Member, preferredTeamCode: String?) : this(
        nickname = member.nickname,
        username = member.username,
        description = member.description,
        preferredTeamCode = preferredTeamCode,
        profileImage = member.profileImage,
    )
}

data class MemberResult(
    val nickname: String,
    val username: String,
    val description: String,
    val profileImage: String,
    val preferredTeamCode: String?,
    val needsOnboarding: Boolean,
) {
    constructor(member: Member, preferredTeamCode: String?) : this(
        nickname = member.nickname,
        username = member.username,
        description = member.description,
        profileImage = member.profileImage,
        preferredTeamCode = preferredTeamCode,
        needsOnboarding = member.isNew(),
    )
}
