package server.image

import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.math.max
import kotlin.math.min

@Component
class WebpThumbnailCompressor {
    fun compress(
        bytes: ByteArray,
        metadata: ImageMetadata,
    ): CompressedImage {
        if (bytes.isEmpty()) {
            throw ImagePlatformException("압축할 이미지 바이트 배열이 비어 있습니다")
        }

        val sourcePath = Files.createTempFile(SOURCE_FILE_PREFIX, ".${metadata.fileExtension}")
        val targetPath = Files.createTempFile(TARGET_FILE_PREFIX, ".$WEBP_FILE_EXTENSION")

        try {
            Files.write(sourcePath, bytes)
            executeVipsCommand(
                sourcePath = sourcePath,
                targetPath = targetPath,
                maxDimension = min(max(metadata.width, metadata.height), MAX_THUMBNAIL_DIMENSION),
            )

            val compressedBytes = Files.readAllBytes(targetPath)
            if (compressedBytes.isEmpty()) {
                throw ImagePlatformException("압축된 썸네일 이미지가 비어 있습니다")
            }

            return CompressedImage(
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
        maxDimension: Int,
    ) {
        val outputPath = "${targetPath}[Q=$WEBP_QUALITY]"
        val process =
            ProcessBuilder(
                VIPS_BINARY,
                "thumbnail",
                sourcePath.toString(),
                outputPath,
                maxDimension.toString(),
                "--height",
                maxDimension.toString(),
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
        private const val MAX_THUMBNAIL_DIMENSION = 320
        private const val SOURCE_FILE_PREFIX = "thumbnail-source-"
        private const val TARGET_FILE_PREFIX = "thumbnail-target-"
    }
}
