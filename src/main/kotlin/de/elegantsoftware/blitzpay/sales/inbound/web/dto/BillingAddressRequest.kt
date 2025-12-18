package de.elegantsoftware.blitzpay.sales.inbound.web.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class BillingAddressRequest(
    @field:NotBlank
    val street: String,

    @field:NotBlank
    val city: String,

    @field:NotBlank
    val postalCode: String,

    @field:NotBlank
    @field:Size(min = 2, max = 2)
    val country: String
)