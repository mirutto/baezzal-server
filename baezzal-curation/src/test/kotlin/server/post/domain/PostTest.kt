package server.post.domain

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class PostTest {
    @Test
    fun `post 생성시 썸네일 기본값은 빈 문자열과 pending 이다`() {
        val post = Post(
            memberId = 1L,
            originalImage = ImageAsset(url = "https://cdn.example.com/post.png"),
        )

        post.originalImage.url shouldBe "https://cdn.example.com/post.png"
        post.thumbnailImage.url shouldBe ""
        post.thumbnailUrl shouldBe ""
        post.thumbnailStatus shouldBe ThumbnailStatus.PENDING
        post.description shouldBe ""
        post.teamId shouldBe null
        post.memberId shouldBe 1L
    }

    @Test
    fun `thumbnail 완료시 thumbnail image 와 status 를 갱신한다`() {
        val post = Post(
            memberId = 1L,
            originalImage = ImageAsset(url = "https://cdn.example.com/post.png"),
        )

        post.completeThumbnail(
            originalImage = ImageAsset(
                url = "https://cdn.example.com/post.png",
                width = 1280,
                height = 720,
                aspectRatio = 1280.0 / 720.0,
            ),
            thumbnailImage = ImageAsset(
                url = "https://static.wowan.me/thumbnails/post.webp",
                width = 320,
                height = 180,
                aspectRatio = 320.0 / 180.0,
            ),
        )

        post.originalImage.width shouldBe 1280
        post.originalImage.height shouldBe 720
        post.thumbnailImage.url shouldBe "https://static.wowan.me/thumbnails/post.webp"
        post.thumbnailImage.width shouldBe 320
        post.thumbnailImage.height shouldBe 180
        post.thumbnailStatus shouldBe ThumbnailStatus.SUCCESS
    }
}
