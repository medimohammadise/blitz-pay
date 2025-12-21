package de.elegantsoftware.blitzpay.invoice.inbound.web.dto

import java.math.BigDecimal
import java.util.UUID

data class AddInvoiceItemWebRequest(
    val invoiceId: Long,
    val productId: Long,
    val productUuid: UUID,
    val merchantProductId: Long,
    val productName: String,
    val productSku: String,
    val description: String? = null,
    val quantity: BigDecimal,
    val unitPrice: BigDecimal,
    val taxRate: BigDecimal = BigDecimal.ZERO,
    val discountPercentage: BigDecimal = BigDecimal.ZERO
)