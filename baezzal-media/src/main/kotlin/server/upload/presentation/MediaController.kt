package server.upload.presentation

import global.auth.Passport
import global.auth.RequestPassport
import global.web.ApiResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import server.upload.application.CreateMediaUploadUrlCommand
import server.upload.application.MediaUploadService
import server.upload.application.MediaUploadUrlResult

@RestController
@RequestMapping("/api/v1/media")
class MediaController(
    private val mediaUploadService: MediaUploadService,
) {
    @PostMapping("/presigned-url")
    fun createUploadUrl(
        @RequestPassport passport: Passport,
        @RequestBody command: CreateMediaUploadUrlCommand,
    ): ApiResponse<MediaUploadUrlResult> = ApiResponse.of(
        mediaUploadService.createUploadUrl(
            memberId = passport.memberId,
            command = command,
        ),
    )
}
