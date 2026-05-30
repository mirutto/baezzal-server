package server.upload.presentation

import global.error.BadRequestException
import global.web.ApiResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import server.upload.application.CreateImageUploadUrlCommand
import server.upload.application.ImageUploadUrlResult
import server.objectstorage.ObjectStorage

@RestController
@RequestMapping("/api/v1/upload")
class UploadController(
    private val objectStorage: ObjectStorage,
) {
    @PostMapping("/image/presigned-url")
    fun createImageUploadUrl(
        @RequestBody command: CreateImageUploadUrlCommand,
    ): ApiResponse<ImageUploadUrlResult> = ApiResponse.of(
        ImageUploadUrlResult.from(
            objectStorage.createPresignedImageUploadUrl(
                fileName = command.fileName.trim().also {
                    if (it.isBlank()) {
                        throw BadRequestException("파일 이름은 비어 있을 수 없습니다")
                    }
                },
                contentType = command.contentType.trim().lowercase().also {
                    if (!it.startsWith(IMAGE_CONTENT_TYPE_PREFIX)) {
                        throw BadRequestException("이미지 파일만 업로드할 수 있습니다")
                    }
                },
            ),
        ),
    )

    companion object {
        private const val IMAGE_CONTENT_TYPE_PREFIX = "image/"
    }
}
