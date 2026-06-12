package server.upload.application

import global.error.BadRequestException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import server.upload.implementation.MediaUploadUrlRecorder
import server.upload.implementation.MediaUploadUrlIssuer
import java.util.UUID

@Service
class MediaUploadService(
    private val mediaUploadUrlIssuer: MediaUploadUrlIssuer,
    private val mediaUploadUrlRecorder: MediaUploadUrlRecorder,
) {
    @Transactional
    fun createUploadUrl(
        memberId: Long,
        command: CreateMediaUploadUrlCommand,
    ): MediaUploadUrlResult {
        require(memberId > 0)
        val prefix = command.prefix.trim().trim('/')
        val contentType = command.contentType.trim().lowercase()

        validatePrefix(prefix)
        validateImageContentType(contentType)

        val uploadUrl =
            mediaUploadUrlIssuer.createPresignedUploadUrl(
                prefix = prefix,
                fileName = createFileName(contentType),
                contentType = contentType,
            )

        mediaUploadUrlRecorder.recordIssued(
            prefix = prefix,
            fileUrl = uploadUrl.fileUrl,
            expiresInSeconds = uploadUrl.expiresInSeconds,
        )

        return MediaUploadUrlResult.from(uploadUrl)
    }

    private fun validatePrefix(prefix: String) {
        if (prefix.isBlank()) {
            throw BadRequestException("prefix 는 비어 있을 수 없습니다")
        }
    }

    private fun validateImageContentType(contentType: String) {
        if (!contentType.startsWith(IMAGE_CONTENT_TYPE_PREFIX)) {
            throw BadRequestException("이미지 파일만 업로드할 수 있습니다")
        }
    }

    private fun createFileName(contentType: String): String =
        "${UUID.randomUUID()}.${extractFileExtension(contentType)}"

    private fun extractFileExtension(contentType: String): String {
        val fileExtension =
            contentType
                .substringBefore(';')
                .substringAfter(IMAGE_CONTENT_TYPE_PREFIX)
                .substringBefore('+')
                .trim()

        if (fileExtension.isBlank()) {
            throw BadRequestException("이미지 파일 확장자를 판별할 수 없습니다")
        }

        return when (fileExtension) {
            JPEG_FILE_EXTENSION -> JPG_FILE_EXTENSION
            else -> fileExtension
        }
    }

    companion object {
        private const val IMAGE_CONTENT_TYPE_PREFIX = "image/"
        private const val JPEG_FILE_EXTENSION = "jpeg"
        private const val JPG_FILE_EXTENSION = "jpg"
    }
}
