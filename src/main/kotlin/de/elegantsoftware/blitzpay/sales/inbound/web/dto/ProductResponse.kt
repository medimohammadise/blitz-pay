package de.elegantsoftware.blitzpay.sales.inbound.web.dto


import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class ProductResponse(
    val id: UUID,
    val merchantId: UUID,
    val merchantName: String?,
    val sku: String,
    val name: String,
    val description: String?,
    val price: BigDecimal,
    val currency: String,
    val stockQuantity: Int,
    val status: String,
    val categories: Set<String>,
    val taxRate: BigDecimal?,
    val isAvailable: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)