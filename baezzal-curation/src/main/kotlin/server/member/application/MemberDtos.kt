package server.member.application

data class MemberOnboardingCommand(
    val nickname: String,
    val preferredTeamId: Long?,
)

data class MemberNicknameUpdateCommand(
    val nickname: String,
)

data class MemberPreferredTeamUpdateCommand(
    val preferredTeamId: Long?,
)

data class MemberData(
    val nickname: String,
    val preferredTeamId: Long?,
)
