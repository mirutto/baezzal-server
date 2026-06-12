package server.my.application

import server.member.domain.Member

data class MyMemberResult(
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

data class MyFollowStats(
    val followerCount: Long,
    val followeeCount: Long,
)
