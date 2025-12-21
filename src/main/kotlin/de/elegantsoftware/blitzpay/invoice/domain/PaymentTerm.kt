package de.elegantsoftware.blitzpay.invoice.domain

enum class PaymentTerm {
    NET_7,
    NET_14,
    NET_30,
    NET_60,
    DUE_ON_RECEIPT,
    CUSTOM
}