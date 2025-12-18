package de.elegantsoftware.blitzpay.merchant.inbound.web.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class CreateMerchantRequest(
    @field:Email
    @field:NotBlank
    val email: String,

    @field:NotBlank
    val businessName: String,

    val defaultCurrency: String = "EUR",
)
