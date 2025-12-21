package de.elegantsoftware.blitzpay.merchant.domain.events

import de.elegantsoftware.blitzpay.merchant.domain.MerchantId
import java.time.LocalDateTime
import java.util.UUID

data class MerchantVerificationEmailRequested(
    val merchantId: MerchantId,
    val publicId: UUID,
    val email: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)