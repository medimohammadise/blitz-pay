package de.elegantsoftware.blitzpay.common.inbound.web.exception

import de.elegantsoftware.blitzpay.common.api.exceptions.*
import de.elegantsoftware.blitzpay.common.inbound.web.exception.dto.ApiErrorResponse
import de.elegantsoftware.blitzpay.common.inbound.web.exception.dto.ValidationError
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.NoHandlerFoundException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(ApiException::class)
    fun handleApiException(
        ex: ApiException,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> {
        log.error("API Exception: ${ex.message}", ex)

        return ResponseEntity
            .status(ex.status)
            .body(
                ApiErrorResponse(
                    status = ex.status.value(),
                    error = ex.status.reasonPhrase,
                    message = ex.message ?: "Unknown error",
                    errorCode = ex.errorCode,
                    path = request.requestURI
                )
            )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> {
        log.warn("Business rule violation: ${ex.message}")

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ApiErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    error = "Business Rule Violation",
                    message = ex.message ?: "Invalid argument provided",
                    path = request.requestURI
                )
            )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> {
        val validationErrors = ex.bindingResult.fieldErrors.map { fieldError ->
            ValidationError(
                field = fieldError.field,
                message = fieldError.defaultMessage ?: "Invalid value",
                rejectedValue = fieldError.rejectedValue
            )
        }

        log.warn("Validation failed: ${validationErrors.joinToString()}")

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ApiErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    error = "Validation Failed",
                    message = "Request validation failed",
                    path = request.requestURI,
                    validationErrors = validationErrors
                )
            )
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(
        ex: ConstraintViolationException,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> {
        val validationErrors = ex.constraintViolations.map { violation ->
            val field = violation.propertyPath.last().name
            ValidationError(
                field = field,
                message = violation.message,
                rejectedValue = violation.invalidValue
            )
        }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ApiErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    error = "Constraint Violation",
                    message = "Request validation failed",
                    path = request.requestURI,
                    validationErrors = validationErrors
                )
            )
    }

    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNoHandlerFound(
        ex: NoHandlerFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                ApiErrorResponse(
                    status = HttpStatus.NOT_FOUND.value(),
                    error = "Not Found",
                    message = "The requested endpoint was not found",
                    path = request.requestURI
                )
            )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ApiErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    error = "Malformed Request",
                    message = "Request body is not readable or malformed",
                    path = request.requestURI
                )
            )
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameter(
        ex: MissingServletRequestParameterException,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ApiErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    error = "Missing Parameter",
                    message = "Required parameter '${ex.parameterName}' is missing",
                    path = request.requestURI
                )
            )
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatch(
        ex: MethodArgumentTypeMismatchException,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ApiErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    error = "Type Mismatch",
                    message = "Parameter '${ex.name}' must be of type ${ex.requiredType?.simpleName}",
                    path = request.requestURI
                )
            )
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(
        ex: AccessDeniedException,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(
                ApiErrorResponse(
                    status = HttpStatus.FORBIDDEN.value(),
                    error = "Access Denied",
                    message = "You don't have permission to access this resource",
                    path = request.requestURI
                )
            )
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> {
        log.error("Unhandled exception occurred", ex)

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ApiErrorResponse(
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    error = "Internal Server Error",
                    message = "An unexpected error occurred. Please try again later.",
                    path = request.requestURI
                )
            )
    }
}