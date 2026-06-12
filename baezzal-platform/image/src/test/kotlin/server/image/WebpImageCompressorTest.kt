package server.image

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import org.junit.jupiter.api.Test
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

class WebpImageCompressorTest {
    private val imageMetadataExtractor = ImageMetadataExtractor()
    private val webpImageCompressor = WebpImageCompressor()

    @Test
    fun `이미지를 지정한 width 이하의 webp 로 압축한다`() {
        val sourceBytes = createPngBytes(width = 640, height = 320)
        val sourceMetadata = imageMetadataExtractor.extract(sourceBytes)
        val maxWidth = 240

        val compressedImage = webpImageCompressor.compress(sourceBytes, sourceMetadata, maxWidth)
        val compressedMetadata = imageMetadataExtractor.extract(compressedImage.bytes)

        compressedImage.bytes shouldNotBe sourceBytes
        compressedImage.contentType shouldBe "image/webp"
        compressedImage.fileExtension shouldBe "webp"
        compressedMetadata.mimeType shouldBe "image/webp"
        compressedMetadata.width shouldBeLessThanOrEqual 240
    }

    @Test
    fun `원본 width 가 기준보다 작으면 width 를 유지한 채 webp 로 압축한다`() {
        val sourceBytes = createPngBytes(width = 200, height = 100)
        val sourceMetadata = imageMetadataExtractor.extract(sourceBytes)

        val compressedImage = webpImageCompressor.compress(sourceBytes, sourceMetadata, maxWidth = 320)
        val compressedMetadata = imageMetadataExtractor.extract(compressedImage.bytes)

        compressedMetadata.width shouldBe 200
        compressedMetadata.height shouldBe 100
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
