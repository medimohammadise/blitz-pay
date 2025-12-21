package de.elegantsoftware.blitzpay.merchant.inbound.web.dto

import java.time.LocalDateTime
import java.util.UUID

data class MerchantResponse(
    val id: UUID,
    val email: String,
    val businessName: String,
    val status: de.elegantsoftware.blitzpay.merchant.domain.MerchantStatus,
    val isActive: Boolean,
    val isEmailVerified: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val settings: MerchantSettingsResponse? = null
)