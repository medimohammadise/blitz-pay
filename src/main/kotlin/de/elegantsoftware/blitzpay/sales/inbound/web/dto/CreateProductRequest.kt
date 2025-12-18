package de.elegantsoftware.blitzpay.sales.inbound.web.dto

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.util.UUID

data class CreateProductRequest(
    @field:NotNull
    val merchantId: UUID,

    @field:NotBlank
    @field:Size(min = 3, max = 50)
    val sku: String,

    @field:NotBlank
    @field:Size(min = 2, max = 200)
    val name: String,

    @field:Size(max = 2000)
    val description: String? = null,

    @field:NotNull
    @field:Positive
    val price: BigDecimal,

    @field:NotBlank
    @field:Size(min = 3, max = 3)
    val currency: String = "EUR",

    @field:Min(0)
    val initialStock: Int = 0,

    @field:PositiveOrZero
    @field:DecimalMax("1.0")
    val taxRate: BigDecimal? = null,

    val categories: Set<String> = emptySet()
)