package de.elegantsoftware.blitzpay.merchant.inbound.web.dto

data class MerchantSettingsResponse(
    val defaultCurrency: String,
    val webhookUrl: String?
)