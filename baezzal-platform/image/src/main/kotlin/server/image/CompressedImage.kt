package server.image

data class CompressedImage(
    val bytes: ByteArray,
    val contentType: String,
    val fileExtension: String,
)
