package de.elegantsoftware.blitzpay.invoice.domain

enum class PaymentMethodType {
    BANK_TRANSFER,
    CREDIT_CARD,
    PAYPAL,
    STRIPE,
    TRUELAYER,
    CASH,
    CHECK,
    OTHER
}