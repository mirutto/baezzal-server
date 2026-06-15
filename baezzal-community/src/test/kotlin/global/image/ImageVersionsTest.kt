package global.image

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ImageVersionsTest {
    @Test
    fun `완료시 public 과 thumbnail 을 채우고 status 를 success 로 바꾼다`() {
        val imageVersions = ImageVersions(
            rawUrl = "https://cdn.example.com/raw.png",
            aspectRatio = 16.0 / 9.0,
        )

        val result = imageVersions.complete(
            publicUrl = " https://cdn.example.com/public.webp ",
            thumbnailUrl = " https://cdn.example.com/thumbnail.webp ",
            aspectRatio = 16.0 / 9.0,
        )

        result.rawUrl shouldBe "https://cdn.example.com/raw.png"
        result.publicUrl shouldBe "https://cdn.example.com/public.webp"
        result.thumbnailUrl shouldBe "https://cdn.example.com/thumbnail.webp"
        result.status shouldBe ImageStatus.SUCCESS
        result.aspectRatio shouldBe 16.0 / 9.0
    }
}
