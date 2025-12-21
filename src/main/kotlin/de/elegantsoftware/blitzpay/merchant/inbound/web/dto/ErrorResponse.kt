package de.elegantsoftware.blitzpay.merchant.inbound.web.dto

import java.time.LocalDateTime

data class ErrorResponse(
    val code: String,
    val message: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)