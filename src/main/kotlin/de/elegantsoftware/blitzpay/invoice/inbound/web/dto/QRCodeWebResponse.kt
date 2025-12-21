package de.elegantsoftware.blitzpay.invoice.inbound.web.dto

data class QRCodeWebResponse(
    val qrCodeData: String,
    val qrCodeImage: String? = null, // Base64 encoded image
    val paymentUrl: String? = null
)