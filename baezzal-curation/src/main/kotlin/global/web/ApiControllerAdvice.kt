package global.web

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.web.HttpMediaTypeNotAcceptableException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import server.token.ExpiredTokenException
import server.token.InvalidTokenException

@Suppress("TooManyFunctions")
@RestControllerAdvice(basePackages = ["server"])
class ApiControllerAdvice {
    data class ErrorResponse(
        val status: Int,
        val message: String,
    )

    @ExceptionHandler(BindException::class)
    fun handleBindException(): ResponseEntity<ErrorResponse> = badRequest("요청 값이 올바르지 않습니다")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handle(): ResponseEntity<ErrorResponse> = badRequest("요청 값이 올바르지 않습니다")

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleNotReadable(): ResponseEntity<ErrorResponse> = badRequest("요청 본문(JSON)이 올바르지 않습니다")

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingValue(): ResponseEntity<ErrorResponse> = badRequest("필수 값이 누락되었습니다")

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotAllowed(): ResponseEntity<ErrorResponse> =
        error(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다")

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleUnsupportedMediaType(): ResponseEntity<ErrorResponse> =
        error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 Content-Type 입니다")

    @ExceptionHandler(HttpMediaTypeNotAcceptableException::class)
    fun handleNotAcceptable(): ResponseEntity<ErrorResponse> =
        error(HttpStatus.NOT_ACCEPTABLE, "지원하지 않는 응답 형식입니다")

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(e: IllegalArgumentException): ResponseEntity<ErrorResponse> =
        badRequest(e.message ?: "잘못된 요청입니다")

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElement(e: NoSuchElementException): ResponseEntity<ErrorResponse> =
        error(HttpStatus.NOT_FOUND, e.message ?: "요청한 리소스를 찾을 수 없습니다")

//    @ExceptionHandler(UnauthorizedException::class)
//    fun handleUnauthorized(
//        request: HttpServletRequest,
//        e: UnauthorizedException
//    ): ResponseEntity<ErrorResponse> {
//        logClientError(request, 401, e.message ?: "LOGIN_AGAIN", e)
//        return error(HttpStatus.UNAUTHORIZED, e.message ?: "LOGIN_AGAIN")
//    }

//    @ExceptionHandler(ForbiddenException::class)
//    fun handleForbidden(
//        request: HttpServletRequest,
//        e: ForbiddenException
//    ): ResponseEntity<ErrorResponse> {
//        return error(HttpStatus.FORBIDDEN, e.message ?: "접근 권한이 없습니다")
//    }

    @ExceptionHandler(InvalidTokenException::class)
    fun handleInvalidToken(e: InvalidTokenException): ResponseEntity<ErrorResponse> =
        error(HttpStatus.UNAUTHORIZED, e.message ?: "INVALID_TOKEN")

    @ExceptionHandler(ExpiredTokenException::class)
    fun handleExpiredToken(e: ExpiredTokenException): ResponseEntity<ErrorResponse> =
        error(HttpStatus.UNAUTHORIZED, e.message ?: "TOKEN_EXPIRED")

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(): ResponseEntity<ErrorResponse> =
        error(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다")

    @ExceptionHandler(Exception::class)
    fun handleException(): ResponseEntity<ErrorResponse> =
        error(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다")

    private fun badRequest(message: String): ResponseEntity<ErrorResponse> =
        error(HttpStatus.BAD_REQUEST, message)

    private fun error(
        status: HttpStatus,
        message: String,
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(status).body(
            ErrorResponse(status = status.value(), message = message),
        )
}
