package server.objectstorage

data class PresignedUploadUrl(
    val objectKey: String,
    val uploadUrl: String,
    val fileUrl: String,
    val headers: Map<String, String>,
    val expiresInSeconds: Int,
)
