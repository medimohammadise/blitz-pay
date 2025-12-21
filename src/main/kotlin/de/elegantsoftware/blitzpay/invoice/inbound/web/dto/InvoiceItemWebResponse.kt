package de.elegantsoftware.blitzpay.invoice.inbound.web.dto

import java.math.BigDecimal
import java.util.UUID

data class InvoiceItemWebResponse(
    val productId: Long,
    val productUuid: UUID,
    val merchantProductId: Long,
    val productName: String,
    val productSku: String,
    val description: String?,
    val quantity: BigDecimal,
    val unitPrice: BigDecimal,
    val taxRate: BigDecimal,
    val discountPercentage: BigDecimal,
    val lineTotal: BigDecimal,
    val taxAmount: BigDecimal,
    val netPrice: BigDecimal
)