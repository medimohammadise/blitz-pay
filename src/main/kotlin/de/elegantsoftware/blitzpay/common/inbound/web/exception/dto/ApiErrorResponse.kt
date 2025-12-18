package de.elegantsoftware.blitzpay.common.inbound.web.exception.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.Instant

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiErrorResponse(
    val timestamp: Instant = Instant.now(),
    val status: Int,
    val error: String,
    val message: String,
    val errorCode: String? = null,
    val path: String? = null,
    val details: Map<String, String>? = null,
    val validationErrors: List<ValidationError>? = null
) {
    constructor(
        status: Int,
        error: String,
        message: String,
        errorCode: String? = null
    ) : this(
        timestamp = Instant.now(),
        status = status,
        error = error,
        message = message,
        errorCode = errorCode
    )
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ValidationError(
    val field: String,
    val message: String,
    val rejectedValue: Any? = null
)