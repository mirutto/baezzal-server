package global.web

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity

class ApiResponse<T> private constructor(
    body: T?,
    status: HttpStatusCode,
) : ResponseEntity<T>(body, status) {
    companion object {
        fun <T> of(
            body: T,
            status: HttpStatusCode = HttpStatus.OK,
        ): ApiResponse<T> = ApiResponse(body, status)

        fun <T> of(status: HttpStatusCode = HttpStatus.OK): ApiResponse<T> =
            ApiResponse(null, status)

        fun <T> error(
            body: T,
            status: HttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR,
        ): ApiResponse<T> = ApiResponse(body, status)
    }
}
