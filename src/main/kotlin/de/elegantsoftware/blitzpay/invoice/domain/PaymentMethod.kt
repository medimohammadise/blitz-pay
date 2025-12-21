package de.elegantsoftware.blitzpay.invoice.domain

data class PaymentMethod(
    val type: PaymentMethodType,
    val details: String? = null,
    val isEnabled: Boolean = true
)