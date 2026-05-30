package global.error

import org.springframework.http.HttpStatus

class InternalServerErrorException(
    message: String,
) : BaseException(HttpStatus.INTERNAL_SERVER_ERROR, message)
