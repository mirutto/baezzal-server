package server.thumbnail.applicaiton

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.image.CompressedImage
import server.image.ImageMetadata
import server.image.ImageUrlReader
import server.thumbnail.implementation.ThumbnailCompressor
import server.thumbnail.implementation.ThumbnailMetadataExtractor
import server.thumbnail.implementation.ThumbnailUploader

class ThumbnailServiceTest {
    private val imageUrlReader = mockk<ImageUrlReader>()
    private val thumbnailMetadataExtractor = mockk<ThumbnailMetadataExtractor>()
    private val thumbnailCompressor = mockk<ThumbnailCompressor>()
    private val thumbnailUploader = mockk<ThumbnailUploader>()
    private val thumbnailService = ThumbnailService(
        imageUrlReader = imageUrlReader,
        thumbnailMetadataExtractor = thumbnailMetadataExtractor,
        thumbnailCompressor = thumbnailCompressor,
        thumbnailUploader = thumbnailUploader,
    )

    @Test
    fun `이미지 추출 압축 업로드 순서로 썸네일 url 을 반환한다`() {
        val imageUrl = "https://cdn.example.com/post-image.png"
        val sourceBytes = "source-image".encodeToByteArray()
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
        val fileName = slot<String>()
        val thumbnailUrl = "https://static.wowan.me/thumbnails/result.webp"

        every { imageUrlReader.readBytes(imageUrl) } returns sourceBytes
        every { thumbnailMetadataExtractor.extract(sourceBytes) } returns metadata
        every { thumbnailCompressor.compress(sourceBytes, metadata) } returns compressedImage
        every { thumbnailUploader.upload(capture(fileName), compressedImage) } returns thumbnailUrl

        val result = thumbnailService.createThumbnail(imageUrl)

        result shouldBe thumbnailUrl
        fileName.captured.substringAfterLast('.') shouldBe "webp"
        verify(exactly = 1) { imageUrlReader.readBytes(imageUrl) }
        verify(exactly = 1) { thumbnailMetadataExtractor.extract(sourceBytes) }
        verify(exactly = 1) { thumbnailCompressor.compress(sourceBytes, metadata) }
        verify(exactly = 1) { thumbnailUploader.upload(any(), compressedImage) }
    }
}
