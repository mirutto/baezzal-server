package server.image

import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.math.min

@Component
class WebpImageCompressor {
    fun compress(
        bytes: ByteArray,
        metadata: ImageMetadata,
        maxWidth: Int,
    ): EncodedImage {
        if (bytes.isEmpty()) {
            throw ImagePlatformException("압축할 이미지 바이트 배열이 비어 있습니다")
        }
        require(maxWidth > 0) { "압축 기준 width 는 0보다 커야 합니다" }

        val sourcePath = Files.createTempFile(SOURCE_FILE_PREFIX, ".${metadata.fileExtension}")
        val targetPath = Files.createTempFile(TARGET_FILE_PREFIX, ".$WEBP_FILE_EXTENSION")

        try {
            Files.write(sourcePath, bytes)
            executeVipsCommand(
                sourcePath = sourcePath,
                targetPath = targetPath,
                maxWidth = min(metadata.width, maxWidth),
            )

            val compressedBytes = Files.readAllBytes(targetPath)
            if (compressedBytes.isEmpty()) {
                throw ImagePlatformException("압축된 썸네일 이미지가 비어 있습니다")
            }

            return EncodedImage(
                bytes = compressedBytes,
                contentType = WEBP_CONTENT_TYPE,
                fileExtension = WEBP_FILE_EXTENSION,
            )
        } finally {
            sourcePath.deleteIfExists()
            targetPath.deleteIfExists()
        }
    }

    private fun executeVipsCommand(
        sourcePath: Path,
        targetPath: Path,
        maxWidth: Int,
    ) {
        val outputPath = "${targetPath}[Q=$WEBP_QUALITY]"
        val process =
            ProcessBuilder(
                VIPS_BINARY,
                "thumbnail",
                sourcePath.toString(),
                outputPath,
                maxWidth.toString(),
                "--size",
                "down",
            ).redirectErrorStream(true)
                .start()

        val output = process.inputStream.bufferedReader().use { it.readText() }.trim()
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            throw ImagePlatformException("vips 썸네일 압축에 실패했습니다: $output")
        }
    }

    companion object {
        private const val VIPS_BINARY = "vips"
        private const val WEBP_CONTENT_TYPE = "image/webp"
        private const val WEBP_FILE_EXTENSION = "webp"
        private const val WEBP_QUALITY = 80
        private const val SOURCE_FILE_PREFIX = "thumbnail-source-"
        private const val TARGET_FILE_PREFIX = "thumbnail-target-"
    }
}
