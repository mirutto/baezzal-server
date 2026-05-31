package server.image

class ImagePlatformException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
