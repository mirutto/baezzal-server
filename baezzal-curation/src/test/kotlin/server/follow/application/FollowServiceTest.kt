package server.follow.application

import global.error.BadRequestException
import global.error.NotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.follow.domain.Follow
import server.follow.implementation.FollowLocker
import server.follow.implementation.FollowReader
import server.follow.implementation.FollowRemover
import server.follow.implementation.FollowWriter
import server.member.domain.Member
import server.member.domain.MemberProvider
import server.member.domain.MemberRole
import server.member.implementation.MemberReader

class FollowServiceTest {
    private val memberReader = mockk<MemberReader>()
    private val followReader = mockk<FollowReader>()
    private val followWriter = mockk<FollowWriter>()
    private val followRemover = mockk<FollowRemover>()
    private val followLocker = mockk<FollowLocker>()
    private val followService = FollowService(
        memberReader = memberReader,
        followReader = followReader,
        followWriter = followWriter,
        followRemover = followRemover,
        followLocker = followLocker,
    )

    init {
        every { followLocker.withLock(any(), any(), any<() -> Any>()) } answers {
            thirdArg<() -> Any>().invoke()
        }
    }

    @Test
    fun `회원을 팔로우한다`() {
        val savedFollow = slot<Follow>()
        every { memberReader.readById(2L) } returns member(id = 2L)
        every { followReader.exists(1L, 2L) } returns false
        every { followWriter.write(capture(savedFollow)) } answers { firstArg() }

        val result = followService.follow(
            followerId = 1L,
            followeeId = 2L,
        )

        result shouldBe FollowResult(
            followerId = 1L,
            followeeId = 2L,
        )
        savedFollow.captured.followerId shouldBe 1L
        savedFollow.captured.followeeId shouldBe 2L
        verify(exactly = 1) { followLocker.withLock(1L, 2L, any<() -> FollowResult>()) }
    }

    @Test
    fun `존재하지 않는 회원은 팔로우할 수 없다`() {
        every { memberReader.readById(2L) } returns null

        shouldThrow<NotFoundException> {
            followService.follow(
                followerId = 1L,
                followeeId = 2L,
            )
        }
    }

    @Test
    fun `이미 팔로우한 회원은 중복 팔로우할 수 없다`() {
        every { memberReader.readById(2L) } returns member(id = 2L)
        every { followReader.exists(1L, 2L) } returns true

        shouldThrow<BadRequestException> {
            followService.follow(
                followerId = 1L,
                followeeId = 2L,
            )
        }
    }

    @Test
    fun `자기 자신은 팔로우할 수 없다`() {
        shouldThrow<BadRequestException> {
            followService.follow(
                followerId = 1L,
                followeeId = 1L,
            )
        }
    }

    @Test
    fun `팔로우를 취소한다`() {
        val follow = Follow(
            id = 3L,
            followerId = 1L,
            followeeId = 2L,
        )
        every { memberReader.readById(2L) } returns member(id = 2L)
        every { followReader.readByFollowerIdAndFolloweeId(1L, 2L) } returns follow
        every { followRemover.remove(follow) } returns Unit

        val result = followService.unfollow(
            followerId = 1L,
            followeeId = 2L,
        )

        result shouldBe FollowResult(
            followerId = 1L,
            followeeId = 2L,
        )
        verify(exactly = 1) { followRemover.remove(follow) }
        verify(exactly = 1) { followLocker.withLock(1L, 2L, any<() -> FollowResult>()) }
    }

    @Test
    fun `팔로우하지 않은 회원은 취소할 수 없다`() {
        every { memberReader.readById(2L) } returns member(id = 2L)
        every { followReader.readByFollowerIdAndFolloweeId(1L, 2L) } returns null

        shouldThrow<BadRequestException> {
            followService.unfollow(
                followerId = 1L,
                followeeId = 2L,
            )
        }
    }

    @Test
    fun `존재하지 않는 회원의 팔로우는 취소할 수 없다`() {
        every { memberReader.readById(2L) } returns null

        shouldThrow<NotFoundException> {
            followService.unfollow(
                followerId = 1L,
                followeeId = 2L,
            )
        }
    }

    private fun member(id: Long): Member = Member(
        id = id,
        nickname = "member-$id",
        provider = MemberProvider.GOOGLE,
        providerKey = "provider-key-$id",
        profileImage = "",
        role = MemberRole.USER,
    )
}
