package server.follow.application

import global.error.BadRequestException
import global.error.NotFoundException
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
        followeeId: Long,
    ): FollowResult = followLocker.withLock(followerId, followeeId) {
        validateFollowee(followerId, followeeId)

        if (followReader.exists(followerId, followeeId)) {
            throw BadRequestException("이미 팔로우한 회원입니다")
        }

        followWriter.write(
            Follow(
                followerId = followerId,
                followeeId = followeeId,
            ),
        )

        FollowResult(
            followerId = followerId,
            followeeId = followeeId,
        )
    }

    @Transactional
    fun unfollow(
        followerId: Long,
        followeeId: Long,
    ): FollowResult = followLocker.withLock(followerId, followeeId) {
        validateFollowee(followerId, followeeId)

        val follow = followReader.readByFollowerIdAndFolloweeId(followerId, followeeId)
            ?: throw BadRequestException("팔로우하지 않은 회원입니다")

        followRemover.remove(follow)

        FollowResult(
            followerId = followerId,
            followeeId = followeeId,
        )
    }

    private fun validateFollowee(
        followerId: Long,
        followeeId: Long,
    ) {
        if (followerId == followeeId) {
            throw BadRequestException("자기 자신은 팔로우할 수 없습니다")
        }

        memberReader.readById(followeeId)
            ?: throw NotFoundException("회원을 찾을 수 없습니다")
    }
}
