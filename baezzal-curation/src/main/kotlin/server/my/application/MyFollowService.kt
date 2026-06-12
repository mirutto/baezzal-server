package server.my.application

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import server.follow.implementation.FollowReader
import server.member.application.MemberData

@Service
class MyFollowService(
    private val followReader: FollowReader,
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
            .map(::MemberData)

    @Transactional(readOnly = true)
    fun getMyFollowings(memberId: Long): List<MemberData> =
        followReader.readFollowingMembers(memberId)
            .map(::MemberData)
}
