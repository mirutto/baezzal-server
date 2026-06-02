package global.error

import org.springframework.http.HttpStatus

open class BaseException(
    val status: HttpStatus,
    message: String,
) : RuntimeException(message)
