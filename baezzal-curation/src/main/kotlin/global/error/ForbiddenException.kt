package global.error

import org.springframework.http.HttpStatus

class ForbiddenException(
    message: String,
) : BaseException(HttpStatus.FORBIDDEN, message)
