package de.elegantsoftware.blitzpay.merchant.inbound.web.dto

import jakarta.validation.constraints.Pattern

data class UpdateSettingsRequest(
    @field:Pattern(regexp = "^[A-Z]{3}$")
    val defaultCurrency: String? = null,

    val transactionFeePercentage: Double? = null,
    val webhookUrl: String? = null
)