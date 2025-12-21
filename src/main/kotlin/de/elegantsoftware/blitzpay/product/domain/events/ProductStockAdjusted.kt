package de.elegantsoftware.blitzpay.product.domain.events

import java.time.Instant
import java.util.*

data class ProductStockAdjusted(
    val productId: UUID,
    val merchantId: Long,
    val oldQuantity: Int,
    val newQuantity: Int,
    val adjustment: Int,
    val changedAt: Instant = Instant.now()
)