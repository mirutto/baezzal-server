package global.web

import global.error.BaseException
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
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

@Suppress("TooManyFunctions")
@RestControllerAdvice(basePackages = ["server"])
class ApiControllerAdvice {
    private val logger = KotlinLogging.logger {}

    data class ErrorResult(
        val status: Int,
        val message: String,
    )

    @ExceptionHandler(BindException::class)
    fun handleBindException(request: HttpServletRequest): ResponseEntity<ErrorResult> =
        badRequest(request, "요청 값이 올바르지 않습니다")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handle(request: HttpServletRequest): ResponseEntity<ErrorResult> =
        badRequest(request, "요청 값이 올바르지 않습니다")

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleNotReadable(request: HttpServletRequest): ResponseEntity<ErrorResult> =
        badRequest(request, "요청 본문(JSON)이 올바르지 않습니다")

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingValue(request: HttpServletRequest): ResponseEntity<ErrorResult> =
        badRequest(request, "필수 값이 누락되었습니다")

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotAllowed(request: HttpServletRequest): ResponseEntity<ErrorResult> =
        error(request, HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다")

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleUnsupportedMediaType(request: HttpServletRequest): ResponseEntity<ErrorResult> =
        error(request, HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 Content-Type 입니다")

    @ExceptionHandler(HttpMediaTypeNotAcceptableException::class)
    fun handleNotAcceptable(request: HttpServletRequest): ResponseEntity<ErrorResult> =
        error(request, HttpStatus.NOT_ACCEPTABLE, "지원하지 않는 응답 형식입니다")

    @ExceptionHandler(BaseException::class)
    fun handleBaseException(
        request: HttpServletRequest,
        e: BaseException,
    ): ResponseEntity<ErrorResult> =
        error(request, e.status, e.message ?: "서버 오류가 발생했습니다", e)

    @ExceptionHandler(Exception::class)
    fun handleException(
        request: HttpServletRequest,
        e: Exception,
    ): ResponseEntity<ErrorResult> =
        error(request, HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다", e)

    private fun badRequest(
        request: HttpServletRequest,
        message: String,
    ): ResponseEntity<ErrorResult> =
        error(request, HttpStatus.BAD_REQUEST, message)

    private fun error(
        request: HttpServletRequest,
        status: HttpStatus,
        message: String,
        exception: Exception? = null,
    ): ResponseEntity<ErrorResult> =
        ResponseEntity.status(status).body(ErrorResult(status = status.value(), message = message)).also {
            log(request, status, message, exception)
        }

    private fun log(
        request: HttpServletRequest,
        status: HttpStatus,
        message: String,
        exception: Exception?,
    ) {
        if (status.is4xxClientError) {
            logger.warn(exception) {
                "${request.method} ${request.requestURI} -> ${status.value()} $message"
            }
        } else {
            logger.error(exception) {
                "${request.method} ${request.requestURI} -> ${status.value()} $message"
            }
        }
    }
}
