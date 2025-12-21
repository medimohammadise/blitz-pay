package de.elegantsoftware.blitzpay.merchant.inbound.web.dto

data class VerificationResponse(
    val success: Boolean,
    val message: String,
    val merchant: MerchantResponse
)