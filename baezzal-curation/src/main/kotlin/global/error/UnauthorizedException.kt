package global.error

import org.springframework.http.HttpStatus

class UnauthorizedException(
    message: String,
) : BaseException(HttpStatus.UNAUTHORIZED, message)
