package server.image

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import com.sun.net.httpserver.HttpServer
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.net.InetSocketAddress
import javax.imageio.ImageIO

class ImageUrlReaderTest {
    private val imageUrlReader = ImageUrlReader()

    @Test
    fun `image url 로부터 바이트 배열을 읽는다`() {
        val expected = createPngBytes(width = 40, height = 20)
        val server = HttpServer.create(InetSocketAddress(0), 0)
        server.createContext("/image.png") { exchange ->
            exchange.responseHeaders.add("Content-Type", "image/png")
            exchange.sendResponseHeaders(200, expected.size.toLong())
            exchange.responseBody.use { it.write(expected) }
        }
        server.start()

        try {
            val actual = imageUrlReader.readBytes("http://127.0.0.1:${server.address.port}/image.png")

            actual shouldBe expected
        } finally {
            server.stop(0)
        }
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
