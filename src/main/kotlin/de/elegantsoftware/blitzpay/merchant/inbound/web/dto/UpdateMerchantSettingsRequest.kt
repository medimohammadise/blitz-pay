package de.elegantsoftware.blitzpay.merchant.inbound.web.dto

data class UpdateMerchantSettingsRequest(
    val defaultCurrency: String = "EUR",
    val language: String = "en",
    val emailNotifications: Boolean = true,
    val smsNotifications: Boolean = false
)