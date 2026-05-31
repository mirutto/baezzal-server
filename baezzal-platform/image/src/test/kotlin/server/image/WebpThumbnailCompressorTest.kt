package server.image

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import org.junit.jupiter.api.Test
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

class WebpThumbnailCompressorTest {
    private val imageMetadataExtractor = ImageMetadataExtractor()
    private val webpThumbnailCompressor = WebpThumbnailCompressor()

    @Test
    fun `이미지를 webp 썸네일로 압축한다`() {
        val sourceBytes = createPngBytes(width = 640, height = 320)
        val sourceMetadata = imageMetadataExtractor.extract(sourceBytes)

        val compressedImage = webpThumbnailCompressor.compress(sourceBytes, sourceMetadata)
        val compressedMetadata = imageMetadataExtractor.extract(compressedImage.bytes)

        compressedImage.bytes shouldNotBe sourceBytes
        compressedImage.contentType shouldBe "image/webp"
        compressedImage.fileExtension shouldBe "webp"
        compressedMetadata.mimeType shouldBe "image/webp"
        compressedMetadata.width shouldBeLessThanOrEqual 320
        compressedMetadata.height shouldBeLessThanOrEqual 320
    }

    private fun createPngBytes(
        width: Int,
        height: Int,
    ): ByteArray {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(image, "png", outputStream)
        return outputStream.toByteArray()
    }
}
