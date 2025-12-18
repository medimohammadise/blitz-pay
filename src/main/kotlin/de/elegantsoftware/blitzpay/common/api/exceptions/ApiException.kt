package de.elegantsoftware.blitzpay.common.api.exceptions

import org.springframework.http.HttpStatus

open class ApiException(
    val status: HttpStatus,
    message: String?,
    val errorCode: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)

open class NotFoundException(
    message: String = "Resource not found",
    errorCode: String? = "RESOURCE_NOT_FOUND"
) : ApiException(HttpStatus.NOT_FOUND, message, errorCode)

open class BadRequestException(
    message: String? = "Bad request",
    errorCode: String? = "BAD_REQUEST"
) : ApiException(HttpStatus.BAD_REQUEST, message, errorCode)

class ValidationException(
    message: String = "Validation failed",
    errorCode: String? = "VALIDATION_FAILED"
) : ApiException(HttpStatus.BAD_REQUEST, message, errorCode)

open class ConflictException(
    message: String = "Conflict occurred",
    errorCode: String? = "CONFLICT"
) : ApiException(HttpStatus.CONFLICT, message, errorCode)

class UnauthorizedException(
    message: String = "Unauthorized",
    errorCode: String? = "UNAUTHORIZED"
) : ApiException(HttpStatus.UNAUTHORIZED, message, errorCode)

class ForbiddenException(
    message: String = "Forbidden",
    errorCode: String? = "FORBIDDEN"
) : ApiException(HttpStatus.FORBIDDEN, message, errorCode)

class InternalServerErrorException(
    message: String = "Internal server error",
    errorCode: String? = "INTERNAL_SERVER_ERROR"
) : ApiException(HttpStatus.INTERNAL_SERVER_ERROR, message, errorCode)