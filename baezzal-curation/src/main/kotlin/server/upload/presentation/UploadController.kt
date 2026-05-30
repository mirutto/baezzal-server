package server.upload.presentation

import global.web.ApiResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import server.upload.application.CreateImageUploadUrlCommand
import server.upload.application.ImageUploadUrlResult
import server.upload.application.UploadService

@RestController
@RequestMapping("/api/v1/upload")
class UploadController(
    private val uploadService: UploadService,
) {
    @PostMapping("/image/presigned-url")
    fun createImageUploadUrl(
        @RequestBody command: CreateImageUploadUrlCommand,
    ): ApiResponse<ImageUploadUrlResult> = ApiResponse.of(
        uploadService.createImageUploadUrl(command),
    )
}
