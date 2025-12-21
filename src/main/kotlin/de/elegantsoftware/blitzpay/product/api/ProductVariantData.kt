package de.elegantsoftware.blitzpay.product.api

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero
import java.math.BigDecimal

data class ProductVariantData(
    @field:NotBlank
    val sku: String,

    @field:NotBlank
    val name: String,

    @field:NotNull
    @field:PositiveOrZero
    val price: BigDecimal,

    val currency: String = "EUR",
    val attributes: Map<String, String> = emptyMap()
)