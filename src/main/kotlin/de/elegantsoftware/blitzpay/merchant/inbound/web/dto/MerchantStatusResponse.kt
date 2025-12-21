package de.elegantsoftware.blitzpay.merchant.inbound.web.dto

data class MerchantStatusResponse(
    val status: de.elegantsoftware.blitzpay.merchant.domain.MerchantStatus,
    val isActive: Boolean,
    val isEmailVerified: Boolean
)