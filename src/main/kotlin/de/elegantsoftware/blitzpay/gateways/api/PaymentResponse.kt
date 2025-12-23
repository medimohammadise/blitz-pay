package de.elegantsoftware.blitzpay.gateways.api

data class PaymentResponse(
    val paymentId: String,
    val authorizationUrl: String?,
    val qrCodeUrl: String?,
    val status: PaymentStatus
)