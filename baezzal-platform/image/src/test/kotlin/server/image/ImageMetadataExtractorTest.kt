package server.image

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

class ImageMetadataExtractorTest {
    private val imageMetadataExtractor = ImageMetadataExtractor()

    @Test
    fun `이미지 메타데이터를 추출한다`() {
        val imageBytes = createPngBytes(width = 120, height = 80)

        val metadata = imageMetadataExtractor.extract(imageBytes)

        metadata.width shouldBe 120
        metadata.height shouldBe 80
        metadata.mimeType shouldBe "image/png"
        metadata.fileExtension shouldBe "png"
        metadata.orientation shouldBe null
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
