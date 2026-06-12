package server.follow.implementation

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.follow.domain.Follow
import server.follow.infrastructure.FollowRepository
import server.member.implementation.MemberReader

@Component
class FollowReader(
    private val followRepository: FollowRepository,
    private val memberReader: MemberReader,
) {
    @Transactional(readOnly = true)
    fun exists(
        followerId: Long,
        followeeId: Long,
    ): Boolean = followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)

    @Transactional(readOnly = true)
    fun readByFollowerIdAndFolloweeId(
        followerId: Long,
        followeeId: Long,
    ): Follow? = followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)

    @Transactional(readOnly = true)
    fun readFollowerCount(memberId: Long) =
        followRepository.countByFolloweeId(memberId)

    @Transactional(readOnly = true)
    fun readFollowingCount(memberId: Long) =
        followRepository.countByFollowerId(memberId)

    @Transactional(readOnly = true)
    fun readFollowerMembers(memberId: Long) =
        readMembers(
            followRepository.findAllByFolloweeId(memberId).map { it.followerId },
        )

    @Transactional(readOnly = true)
    fun readFollowingMembers(memberId: Long) =
        readMembers(
            followRepository.findAllByFollowerId(memberId).map { it.followeeId },
        )

    private fun readMembers(memberIds: List<Long>) =
        memberReader.readByIds(memberIds)
            .associateBy { it.id }
            .let { membersById ->
                memberIds.mapNotNull(membersById::get)
            }
}
