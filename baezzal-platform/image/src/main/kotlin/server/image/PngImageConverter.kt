package server.image

import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists

@Component
class PngImageConverter {
    fun convert(
        bytes: ByteArray,
        metadata: ImageMetadata,
    ): EncodedImage {
        if (bytes.isEmpty()) {
            throw ImagePlatformException("변환할 이미지 바이트 배열이 비어 있습니다")
        }
        if (metadata.mimeType == PNG_CONTENT_TYPE) {
            return EncodedImage(
                bytes = bytes,
                contentType = PNG_CONTENT_TYPE,
                fileExtension = PNG_FILE_EXTENSION,
            )
        }

        val sourcePath = Files.createTempFile(SOURCE_FILE_PREFIX, ".${metadata.fileExtension}")
        val targetPath = Files.createTempFile(TARGET_FILE_PREFIX, ".$PNG_FILE_EXTENSION")

        try {
            Files.write(sourcePath, bytes)
            executeVipsCommand(
                sourcePath = sourcePath,
                targetPath = targetPath,
                width = metadata.width,
            )

            val convertedBytes = Files.readAllBytes(targetPath)
            if (convertedBytes.isEmpty()) {
                throw ImagePlatformException("변환된 PNG 이미지가 비어 있습니다")
            }

            return EncodedImage(
                bytes = convertedBytes,
                contentType = PNG_CONTENT_TYPE,
                fileExtension = PNG_FILE_EXTENSION,
            )
        } finally {
            sourcePath.deleteIfExists()
            targetPath.deleteIfExists()
        }
    }

    private fun executeVipsCommand(
        sourcePath: Path,
        targetPath: Path,
        width: Int,
    ) {
        val process =
            ProcessBuilder(
                VIPS_BINARY,
                "thumbnail",
                sourcePath.toString(),
                targetPath.toString(),
                width.toString(),
                "--size",
                "down",
            ).redirectErrorStream(true)
                .start()

        val output = process.inputStream.bufferedReader().use { it.readText() }.trim()
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            throw ImagePlatformException("vips PNG 변환에 실패했습니다: $output")
        }
    }

    companion object {
        private const val VIPS_BINARY = "vips"
        private const val PNG_CONTENT_TYPE = "image/png"
        private const val PNG_FILE_EXTENSION = "png"
        private const val SOURCE_FILE_PREFIX = "png-source-"
        private const val TARGET_FILE_PREFIX = "png-target-"
    }
}
