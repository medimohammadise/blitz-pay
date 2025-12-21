package de.elegantsoftware.blitzpay.merchant.domain

enum class MerchantStatus {
    PENDING_VERIFICATION,
    PENDING_EMAIL_VERIFICATION,
    ACTIVE,
    INACTIVE,
    SUSPENDED
}