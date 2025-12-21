package de.elegantsoftware.blitzpay.invoice.inbound.web.dto

data class PaymentMethodWebResponse(
    val type: String,
    val details: String?,
    val isEnabled: Boolean
)