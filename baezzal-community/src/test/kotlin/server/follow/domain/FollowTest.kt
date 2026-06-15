package server.follow.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class FollowTest {
    @Test
    fun `서로 다른 멤버 사이에 팔로우를 생성한다`() {
        val follow = Follow(
            followerId = 1L,
            followeeId = 2L,
        )

        follow.followerId shouldBe 1L
        follow.followeeId shouldBe 2L
    }

    @Test
    fun `자기 자신을 팔로우할 수 없다`() {
        shouldThrow<IllegalArgumentException> {
            Follow(
                followerId = 1L,
                followeeId = 1L,
            )
        }
    }
}
