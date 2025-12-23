package de.elegantsoftware.blitzpay.payment.inbound.web.dto

import java.util.UUID

data class QRCodeResponse(
    val paymentId: UUID,
    val qrCodeBase64: String,
    val qrCodeUrl: String?,
    val paymentUrl: String
)