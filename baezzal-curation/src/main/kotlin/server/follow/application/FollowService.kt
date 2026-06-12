package server.follow.application

import global.error.BadRequestException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import server.follow.domain.Follow
import server.follow.implementation.FollowLocker
import server.follow.implementation.FollowReader
import server.follow.implementation.FollowRemover
import server.follow.implementation.FollowWriter
import server.member.implementation.MemberReader

@Service
class FollowService(
    private val memberReader: MemberReader,
    private val followReader: FollowReader,
    private val followWriter: FollowWriter,
    private val followRemover: FollowRemover,
    private val followLocker: FollowLocker,
) {
    @Transactional
    fun follow(
        followerId: Long,
        followeeUsername: String,
    ): FollowResult {
        val followee = validateFollowee(followerId, followeeUsername)

        return followLocker.withLock(followerId, followee.id) {
            if (followReader.exists(followerId, followee.id)) {
                throw BadRequestException("이미 팔로우한 회원입니다")
            }

            followWriter.write(
                Follow(
                    followerId = followerId,
                    followeeId = followee.id,
                ),
            )

            FollowResult(
                followerId = followerId,
                followeeId = followee.id,
            )
        }
    }

    @Transactional
    fun unfollow(
        followerId: Long,
        followeeUsername: String,
    ): FollowResult {
        val followee = validateFollowee(followerId, followeeUsername)

        return followLocker.withLock(followerId, followee.id) {
            val follow = followReader.readByFollowerIdAndFolloweeId(followerId, followee.id)
                ?: throw BadRequestException("팔로우하지 않은 회원입니다")

            followRemover.remove(follow)

            FollowResult(
                followerId = followerId,
                followeeId = followee.id,
            )
        }
    }

    private fun validateFollowee(
        followerId: Long,
        followeeUsername: String,
    ) = memberReader.readByUsername(followeeUsername).also { followee ->
        if (followerId == followee.id) {
            throw BadRequestException("자기 자신은 팔로우할 수 없습니다")
        }
    }

    @Transactional(readOnly = true)
    fun getMemberFollowSummary(
        memberId: Long,
        targetUsername: String,
    ): MemberFollowSummaryResult {
        val targetMember = memberReader.readByUsername(targetUsername)
        val followerCount = followReader.readFollowerCount(targetMember.id)

        if (memberId == targetMember.id) {
            return MemberFollowSummaryResult(
                followerCount = followerCount,
                isFollowing = false,
            )
        }

        return MemberFollowSummaryResult(
            followerCount = followerCount,
            isFollowing = followReader.exists(memberId, targetMember.id),
        )
    }
}
