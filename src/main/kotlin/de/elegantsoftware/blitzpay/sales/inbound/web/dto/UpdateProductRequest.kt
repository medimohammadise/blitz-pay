package de.elegantsoftware.blitzpay.sales.inbound.web.dto

import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class UpdateProductRequest(
    @field:Size(min = 2, max = 200)
    val name: String? = null,

    @field:Size(max = 2000)
    val description: String? = null,

    @field:Positive
    val price: BigDecimal? = null,

    @field:PositiveOrZero
    val stockAdjustment: Int? = null
)