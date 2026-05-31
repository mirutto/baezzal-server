package server.image

import org.springframework.stereotype.Component
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.io.IOException

@Component
class ImageUrlReader {
    private val httpClient: HttpClient =
        HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(CONNECT_TIMEOUT)
            .build()

    fun readBytes(imageUrl: String): ByteArray {
        if (imageUrl.isBlank()) {
            throw ImagePlatformException("imageUrl 은 비어 있을 수 없습니다")
        }

        val request =
            try {
                HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl))
                    .timeout(READ_TIMEOUT)
                    .GET()
                    .build()
            } catch (exception: IllegalArgumentException) {
                throw ImagePlatformException("유효하지 않은 imageUrl 입니다", exception)
            }

        val response =
            try {
                httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray())
            } catch (exception: IOException) {
                throw ImagePlatformException("imageUrl 로부터 이미지를 읽을 수 없습니다", exception)
            } catch (exception: InterruptedException) {
                Thread.currentThread().interrupt()
                throw ImagePlatformException("imageUrl 요청이 중단되었습니다", exception)
            }

        if (response.statusCode() !in SUCCESS_STATUS_RANGE) {
            throw ImagePlatformException("imageUrl 응답이 성공하지 않았습니다: ${response.statusCode()}")
        }

        return response.body()
            .takeIf(ByteArray::isNotEmpty)
            ?: throw ImagePlatformException("imageUrl 응답 본문이 비어 있습니다")
    }

    companion object {
        private val CONNECT_TIMEOUT: Duration = Duration.ofSeconds(5)
        private val READ_TIMEOUT: Duration = Duration.ofSeconds(10)
        private val SUCCESS_STATUS_RANGE: IntRange = 200..299
    }
}
