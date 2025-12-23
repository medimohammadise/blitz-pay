package de.elegantsoftware.blitzpay.product.domain.events

import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Instant

data class ProductArchived(
    val productId: UUID,
    val merchantId: Long,
    val archivedAt: Instant = Clock.System.now()
)
