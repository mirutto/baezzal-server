package server.image

data class EncodedImage(
    val bytes: ByteArray,
    val contentType: String,
    val fileExtension: String,
)
