package server.post.application

import server.objectstorage.PresignedUploadUrl

data class CreatePostImageUploadUrlCommand(
    val contentType: String,
)

data class PostImageUploadUrlResult(
    val objectKey: String,
    val uploadUrl: String,
    val fileUrl: String,
    val headers: Map<String, String>,
    val expiresInSeconds: Int,
) {
    companion object {
        fun from(uploadUrl: PresignedUploadUrl): PostImageUploadUrlResult =
            PostImageUploadUrlResult(
                objectKey = uploadUrl.objectKey,
                uploadUrl = uploadUrl.uploadUrl,
                fileUrl = uploadUrl.fileUrl,
                headers = uploadUrl.headers,
                expiresInSeconds = uploadUrl.expiresInSeconds,
            )
    }
}
