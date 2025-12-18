package de.elegantsoftware.blitzpay.sales.inbound.web.dto

import java.math.BigDecimal
import java.util.UUID

data class InvoiceItemResponse(
    val id: UUID,
    val product: ProductRefResponse,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val currency: String,
    val lineTotal: BigDecimal,
    val taxAmount: BigDecimal?
)

data class ProductRefResponse(
    val id: UUID,
    val sku: String,
    val name: String
)