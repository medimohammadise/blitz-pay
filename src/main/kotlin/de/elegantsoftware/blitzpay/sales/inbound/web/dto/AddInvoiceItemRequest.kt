package de.elegantsoftware.blitzpay.sales.inbound.web.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.util.UUID

data class AddInvoiceItemRequest(
    @field:NotNull
    val productId: UUID,

    @field:Min(1)
    val quantity: Int,

    @field:Positive
    val unitPrice: BigDecimal? = null
)