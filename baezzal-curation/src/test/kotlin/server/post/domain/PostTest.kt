package server.post.domain

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class PostTest {
    @Test
    fun `post 생성시 썸네일 기본값은 빈 문자열과 pending 이다`() {
        val post = Post(memberId = 1L, imageUrl = "https://cdn.example.com/post.png")

        post.thumbnailUrl shouldBe ""
        post.thumbnailStatus shouldBe ThumbnailStatus.PENDING
        post.description shouldBe ""
        post.teamId shouldBe null
        post.memberId shouldBe 1L
    }
}
