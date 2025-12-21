package de.elegantsoftware.blitzpay.product.inbound.web.dto

import de.elegantsoftware.blitzpay.product.domain.Price
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero

data class ProductVariantRequest(
    @field:NotBlank
    val sku: String,

    @field:NotBlank
    val name: String,

    @field:NotNull
    @field:PositiveOrZero
    val price: Price,

    val currency: String = "EUR",
    val attributes: Map<String, String> = emptyMap()
)