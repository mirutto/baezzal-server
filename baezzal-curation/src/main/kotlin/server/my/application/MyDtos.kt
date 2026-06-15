package server.my.application

import server.member.domain.Member

data class MyMemberResult(
    val nickname: String,
    val username: String,
    val description: String,
    val publicProfileImageUrl: String,
    val thumbnailProfileImageUrl: String,
    val preferredTeamCode: String?,
    val needsOnboarding: Boolean,
) {
    constructor(member: Member, preferredTeamCode: String?) : this(
        nickname = member.nickname,
        username = member.username,
        description = member.description,
        publicProfileImageUrl = member.profileImage.publicUrl,
        thumbnailProfileImageUrl = member.profileImage.thumbnailUrl,
        preferredTeamCode = preferredTeamCode,
        needsOnboarding = member.isNew(),
    )
}

data class MyFollowStats(
    val followerCount: Long,
    val followeeCount: Long,
)
