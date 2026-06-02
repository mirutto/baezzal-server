package server.upload.application

data class MediaUploadUrlIssuedEvent(
    val prefix: String,
    val objectKey: String,
    val fileUrl: String,
    val expiresInSeconds: Int,
)
