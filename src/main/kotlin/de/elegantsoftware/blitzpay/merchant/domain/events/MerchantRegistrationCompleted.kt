package de.elegantsoftware.blitzpay.merchant.domain.events

import de.elegantsoftware.blitzpay.merchant.domain.MerchantId
import java.util.UUID

data class MerchantRegistrationCompleted(
    val merchantId: MerchantId,
    val publicId: UUID
)