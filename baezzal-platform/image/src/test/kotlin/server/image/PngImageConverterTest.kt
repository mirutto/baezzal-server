package server.image

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

class PngImageConverterTest {
    private val imageMetadataExtractor = ImageMetadataExtractor()
    private val pngImageConverter = PngImageConverter()

    @Test
    fun `jpeg 이미지를 png 로 변환한다`() {
        val sourceBytes = createJpegBytes(width = 640, height = 320)
        val sourceMetadata = imageMetadataExtractor.extract(sourceBytes)

        val convertedImage = pngImageConverter.convert(sourceBytes, sourceMetadata)
        val convertedMetadata = imageMetadataExtractor.extract(convertedImage.bytes)

        convertedImage.bytes shouldNotBe sourceBytes
        convertedImage.contentType shouldBe "image/png"
        convertedImage.fileExtension shouldBe "png"
        convertedMetadata.mimeType shouldBe "image/png"
        convertedMetadata.width shouldBe 640
        convertedMetadata.height shouldBe 320
    }

    @Test
    fun `기존 이미지가 png 이면 그대로 반환한다`() {
        val sourceBytes = createPngBytes(width = 200, height = 100)
        val sourceMetadata = imageMetadataExtractor.extract(sourceBytes)

        val convertedImage = pngImageConverter.convert(sourceBytes, sourceMetadata)

        convertedImage.bytes shouldBe sourceBytes
        convertedImage.contentType shouldBe "image/png"
        convertedImage.fileExtension shouldBe "png"
    }

    private fun createJpegBytes(
        width: Int,
        height: Int,
    ): ByteArray {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(image, "jpeg", outputStream)
        return outputStream.toByteArray()
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
