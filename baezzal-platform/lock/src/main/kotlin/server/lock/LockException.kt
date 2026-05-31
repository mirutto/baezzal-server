package server.lock

class LockException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
