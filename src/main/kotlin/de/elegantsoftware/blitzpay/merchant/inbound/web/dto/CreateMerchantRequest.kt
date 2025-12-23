package de.elegantsoftware.blitzpay.merchant.inbound.web.dto

data class CreateMerchantRequest(
    val email: String,
    val businessName: String? = null,
    val defaultCurrency: String? = null,
    val language: String? = null,
    val emailNotifications: Boolean? = null,
    val smsNotifications: Boolean? = null
)