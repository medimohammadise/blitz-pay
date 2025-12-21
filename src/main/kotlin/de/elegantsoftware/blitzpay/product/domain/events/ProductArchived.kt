package de.elegantsoftware.blitzpay.product.domain.events

import java.time.Instant
import java.util.*

data class ProductArchived(
    val productId: UUID,
    val merchantId: Long,
    val archivedAt: kotlin.time.Instant = Instant.now()
)
