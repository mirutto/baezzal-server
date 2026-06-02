package server.upload.application

import server.objectstorage.PresignedUploadUrl

data class CreateMediaUploadUrlCommand(
    val prefix: String,
    val contentType: String,
)

data class MediaUploadUrlResult(
    val objectKey: String,
    val uploadUrl: String,
    val fileUrl: String,
    val headers: Map<String, String>,
    val expiresInSeconds: Int,
) {
    companion object {
        fun from(uploadUrl: PresignedUploadUrl): MediaUploadUrlResult =
            MediaUploadUrlResult(
                objectKey = uploadUrl.objectKey,
                uploadUrl = uploadUrl.uploadUrl,
                fileUrl = uploadUrl.fileUrl,
                headers = uploadUrl.headers,
                expiresInSeconds = uploadUrl.expiresInSeconds,
            )
    }
}
