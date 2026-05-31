package server.thumbnail.implementation

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.image.ImageMetadata
import server.image.ImageMetadataExtractor

class ThumbnailMetadataExtractorTest {
    private val imageMetadataExtractor = mockk<ImageMetadataExtractor>()
    private val thumbnailMetadataExtractor = ThumbnailMetadataExtractor(imageMetadataExtractor)

    @Test
    fun `이미지 메타데이터 추출을 위임한다`() {
        val imageBytes = "source-image".encodeToByteArray()
        val metadata = ImageMetadata(
            width = 1280,
            height = 720,
            mimeType = "image/png",
            fileExtension = "png",
            orientation = 1,
        )

        every { imageMetadataExtractor.extract(imageBytes) } returns metadata

        val result = thumbnailMetadataExtractor.extract(imageBytes)

        result shouldBe metadata
        verify(exactly = 1) { imageMetadataExtractor.extract(imageBytes) }
    }
}
