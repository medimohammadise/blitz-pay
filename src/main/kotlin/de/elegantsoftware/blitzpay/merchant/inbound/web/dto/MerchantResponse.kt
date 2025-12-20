package de.elegantsoftware.blitzpay.merchant.inbound.web.dto

import java.time.LocalDateTime
import java.util.UUID

data class MerchantResponse(
    val id: Long,
    val publicId: UUID,
    val email: String,
    val businessName: String,
    val status: String,
    val settings: MerchantSettingsResponse,
    val verifiedAt: LocalDateTime?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)