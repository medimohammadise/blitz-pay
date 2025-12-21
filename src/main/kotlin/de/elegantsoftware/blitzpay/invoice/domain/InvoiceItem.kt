package de.elegantsoftware.blitzpay.invoice.domain

import java.math.BigDecimal
import java.util.UUID

data class InvoiceItem(
    val productId: Long,
    val productUuid: UUID,
    val merchantProductId: Long, // Reference to merchant's product catalog
    val productName: String,
    val productSku: String,
    val description: String? = null,
    val quantity: BigDecimal,
    val unitPrice: BigDecimal,
    val taxRate: BigDecimal = BigDecimal.ZERO,
    val discountPercentage: BigDecimal = BigDecimal.ZERO
) {
    val lineTotal: BigDecimal
        get() = (unitPrice * quantity) * (BigDecimal.ONE - discountPercentage.divide(BigDecimal(100)))

    val taxAmount: BigDecimal
        get() = lineTotal * (taxRate.divide(BigDecimal(100)))

    val netPrice: BigDecimal
        get() = lineTotal - taxAmount
}