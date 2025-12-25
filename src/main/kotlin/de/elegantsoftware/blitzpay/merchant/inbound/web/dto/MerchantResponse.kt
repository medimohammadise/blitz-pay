package de.elegantsoftware.blitzpay.merchant.inbound.web.dto

import de.elegantsoftware.blitzpay.merchant.domain.MerchantStatus
import java.util.UUID
import kotlin.time.Instant

data class MerchantResponse(
    val id: UUID,
    val email: String,
    val businessName: String?,
    val status: MerchantStatus,
    val isActive: Boolean,
    val isEmailVerified: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
    val settings: MerchantSettingsResponse? = null
)