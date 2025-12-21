package de.elegantsoftware.blitzpay.product.domain.events

import java.math.BigDecimal
import java.time.Instant
import java.util.*

data class ProductPriceChanged(
    val productId: UUID,
    val merchantId: Long,
    val oldPrice: BigDecimal,
    val oldCurrency: String,
    val newPrice: BigDecimal,
    val newCurrency: String,
    val changedAt: Instant = Instant.now()
)