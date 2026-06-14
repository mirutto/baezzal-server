package server.my.application

import server.member.domain.Member

data class MyMemberResult(
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

data class MyFollowStats(
    val followerCount: Long,
    val followeeCount: Long,
)
