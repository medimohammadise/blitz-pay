package de.elegantsoftware.blitzpay.merchant.domain.events

import de.elegantsoftware.blitzpay.merchant.domain.MerchantId
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Instant

data class MerchantVerificationEmailRequested(
    val merchantId: MerchantId,
    val publicId: UUID,
    val email: String,
    val timestamp: Instant = Clock.System.now()
)