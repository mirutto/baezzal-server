package server.upload.application

import global.error.BadRequestException
import org.springframework.stereotype.Service
import server.image.upload.PresignedImageUploadUrlService
import server.upload.application.ImageUploadUrlResult.Companion.from

@Service
class UploadService(
    private val presignedImageUploadUrlService: PresignedImageUploadUrlService,
) {
    fun createImageUploadUrl(command: CreateImageUploadUrlCommand): ImageUploadUrlResult {
        val fileName = command.fileName.trim()
        val contentType = command.contentType.trim().lowercase()

        if (fileName.isBlank()) {
            throw BadRequestException("파일 이름은 비어 있을 수 없습니다")
        }

        if (!contentType.startsWith(IMAGE_CONTENT_TYPE_PREFIX)) {
            throw BadRequestException("이미지 파일만 업로드할 수 있습니다")
        }

        return from(
            presignedImageUploadUrlService.issue(
                fileName = fileName,
                contentType = contentType,
            ),
        )
    }

    companion object {
        private const val IMAGE_CONTENT_TYPE_PREFIX = "image/"
    }
}
