package server.member.application

data class MediaUploadUrlIssuedEvent(
    val prefix: String,
    val objectKey: String,
    val fileUrl: String,
    val expiresInSeconds: Int,
)
