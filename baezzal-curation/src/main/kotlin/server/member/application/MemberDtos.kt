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
    val publicProfileImageUrl: String,
    val thumbnailProfileImageUrl: String,
) {
    constructor(member: Member, preferredTeamCode: String?) : this(
        nickname = member.nickname,
        username = member.username,
        description = member.description,
        preferredTeamCode = preferredTeamCode,
        publicProfileImageUrl = member.profileImage.publicUrl,
        thumbnailProfileImageUrl = member.profileImage.thumbnailUrl,
    )
}

data class MemberIdResult(
    val memberId: Long,
) {
    constructor(member: Member) : this(memberId = member.id)
}
