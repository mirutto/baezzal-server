package server.thumbnail.implementation

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.image.CompressedImage
import server.objectstorage.ObjectStorage

class ThumbnailUploaderTest {
    private val objectStorage = mockk<ObjectStorage>()
    private val thumbnailUploader = ThumbnailUploader(objectStorage)

    @Test
    fun `webp 썸네일을 object storage 에 업로드한다`() {
        val image = CompressedImage(
            bytes = "thumbnail".encodeToByteArray(),
            contentType = "image/webp",
            fileExtension = "webp",
        )
        every {
            objectStorage.uploadImage(
                prefix = "thumbnails",
                fileName = "sample.webp",
                contentType = "image/webp",
                inputStream = any(),
                size = image.bytes.size.toLong(),
            )
        } returns "https://static.wowan.me/thumbnails/sample.webp"

        val result = thumbnailUploader.upload("sample.webp", image)

        result shouldBe "https://static.wowan.me/thumbnails/sample.webp"
        verify(exactly = 1) {
            objectStorage.uploadImage(
                prefix = "thumbnails",
                fileName = "sample.webp",
                contentType = "image/webp",
                inputStream = any(),
                size = image.bytes.size.toLong(),
            )
        }
    }
}
