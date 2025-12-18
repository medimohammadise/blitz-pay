package de.elegantsoftware.blitzpay.merchant.domain.events

import java.time.LocalDateTime
import java.util.UUID

data class MerchantRegisteredEvent(
    val merchantPublicId: UUID?,
    val email: String?,
    val businessName: String?,
    val occurredAt: LocalDateTime = LocalDateTime.now()
)