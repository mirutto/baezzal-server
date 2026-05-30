package server.upload.application

import global.error.BadRequestException
import org.springframework.stereotype.Service
import server.upload.implementation.UploadImageUploader

@Service
class UploadService(
    private val uploadImageUploader: UploadImageUploader,
) {
    fun createImageUploadUrl(command: CreateImageUploadUrlCommand): ImageUploadUrlResult {
        val prefix = command.prefix.trim()
        val fileName = command.fileName.trim()
        val contentType = command.contentType.trim().lowercase()
        validate(prefix, fileName, contentType)

        return ImageUploadUrlResult.from(
            uploadImageUploader.createPresignedUploadUrl(
                prefix = prefix,
                fileName = fileName,
                contentType = contentType,
            ),
        )
    }

    companion object {
        private const val IMAGE_CONTENT_TYPE_PREFIX = "image/"
    }

    private fun validate(
        prefix: String,
        fileName: String,
        contentType: String,
    ) {
        if (prefix.isBlank()) {
            throw BadRequestException("prefix 는 비어 있을 수 없습니다")
        }

        if (fileName.isBlank()) {
            throw BadRequestException("파일 이름은 비어 있을 수 없습니다")
        }

        if (!contentType.startsWith(IMAGE_CONTENT_TYPE_PREFIX)) {
            throw BadRequestException("이미지 파일만 업로드할 수 있습니다")
        }
    }
}
