package server.upload.application

import server.image.upload.PresignedImageUploadUrl

data class CreateImageUploadUrlCommand(
    val fileName: String,
    val contentType: String,
)

data class ImageUploadUrlResult(
    val objectKey: String,
    val uploadUrl: String,
    val fileUrl: String,
    val headers: Map<String, String>,
    val expiresInSeconds: Int,
) {
    companion object {
        fun from(imageUploadUrl: PresignedImageUploadUrl): ImageUploadUrlResult =
            ImageUploadUrlResult(
                objectKey = imageUploadUrl.objectKey,
                uploadUrl = imageUploadUrl.uploadUrl,
                fileUrl = imageUploadUrl.fileUrl,
                headers = imageUploadUrl.headers,
                expiresInSeconds = imageUploadUrl.expiresInSeconds,
            )
    }
}
