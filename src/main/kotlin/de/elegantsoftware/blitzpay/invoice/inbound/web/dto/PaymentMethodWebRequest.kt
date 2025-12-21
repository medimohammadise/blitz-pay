package de.elegantsoftware.blitzpay.invoice.inbound.web.dto

data class PaymentMethodWebRequest(
    val type: String,
    val details: String? = null,
    val isEnabled: Boolean = true
)