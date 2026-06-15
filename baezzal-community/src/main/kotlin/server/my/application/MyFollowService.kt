package server.my.application

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import server.follow.implementation.FollowReader
import server.member.application.MemberData
import server.team.implementation.TeamReader

@Service
class MyFollowService(
    private val followReader: FollowReader,
    private val teamReader: TeamReader,
) {
    @Transactional(readOnly = true)
    fun getMyStats(memberId: Long): MyFollowStats {
        val followerCount = followReader.readFollowerCount(memberId)
        val followeeCount = followReader.readFollowingCount(memberId)

        return MyFollowStats(followerCount, followeeCount)
    }

    @Transactional(readOnly = true)
    fun getMyFollowers(memberId: Long): List<MemberData> =
        followReader.readFollowerMembers(memberId)
            .map { member ->
                MemberData(member, teamReader.resolveCode(member.preferredTeamId))
            }

    @Transactional(readOnly = true)
    fun getMyFollowings(memberId: Long): List<MemberData> =
        followReader.readFollowingMembers(memberId)
            .map { member ->
                MemberData(member, teamReader.resolveCode(member.preferredTeamId))
            }
}
