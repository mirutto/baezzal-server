package server.post.domain

import global.image.ImageStatus
import global.image.ImageVersions
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class PostTest {
    @Test
    fun `post 생성시 image 기본값은 processing 이다`() {
        val post = Post(
            memberId = 1L,
            image = ImageVersions(rawUrl = "https://cdn.example.com/post.png"),
        )

        post.rawImageUrl shouldBe "https://cdn.example.com/post.png"
        post.imageUrl shouldBe "https://cdn.example.com/post.png"
        post.thumbnailUrl shouldBe ""
        post.imageStatus shouldBe ImageStatus.PROCESSING
        post.description shouldBe ""
        post.teamId shouldBe null
        post.viewCount shouldBe 0L
        post.memberId shouldBe 1L
    }

    @Test
    fun `image 완료시 public 과 thumbnail 과 status 를 갱신한다`() {
        val post = Post(
            memberId = 1L,
            image = ImageVersions(rawUrl = "https://cdn.example.com/post.png"),
        )

        post.completeImage(
            publicUrl = " https://cdn.example.com/post.webp ",
            thumbnailUrl = " https://static.wowan.me/thumbnails/post.webp ",
            aspectRatio = 16.0 / 9.0,
        )

        post.rawImageUrl shouldBe "https://cdn.example.com/post.png"
        post.image.publicUrl shouldBe "https://cdn.example.com/post.webp"
        post.thumbnailUrl shouldBe "https://static.wowan.me/thumbnails/post.webp"
        post.imageStatus shouldBe ImageStatus.SUCCESS
        post.image.aspectRatio shouldBe 16.0 / 9.0
    }

    @Test
    fun `view count 를 증가시킨다`() {
        val post = Post(
            memberId = 1L,
            image = ImageVersions(rawUrl = "https://cdn.example.com/post.png"),
        )

        post.increaseViewCount(3L)

        post.viewCount shouldBe 3L
    }
}
