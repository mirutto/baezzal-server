package server.thumbnail.implementation

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.image.CompressedImage
import server.image.ImageMetadata
import server.image.WebpThumbnailCompressor

class ThumbnailCompressorTest {
    private val webpThumbnailCompressor = mockk<WebpThumbnailCompressor>()
    private val thumbnailCompressor = ThumbnailCompressor(webpThumbnailCompressor)

    @Test
    fun `썸네일 압축을 위임한다`() {
        val imageBytes = "source-image".encodeToByteArray()
        val metadata = ImageMetadata(
            width = 1280,
            height = 720,
            mimeType = "image/png",
            fileExtension = "png",
            orientation = 1,
        )
        val compressedImage = CompressedImage(
            bytes = "webp-thumbnail".encodeToByteArray(),
            contentType = "image/webp",
            fileExtension = "webp",
        )

        every { webpThumbnailCompressor.compress(imageBytes, metadata) } returns compressedImage

        val result = thumbnailCompressor.compress(imageBytes, metadata)

        result shouldBe compressedImage
        verify(exactly = 1) { webpThumbnailCompressor.compress(imageBytes, metadata) }
    }
}
