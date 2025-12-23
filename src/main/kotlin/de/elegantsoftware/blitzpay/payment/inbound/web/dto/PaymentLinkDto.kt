package de.elegantsoftware.blitzpay.payment.inbound.web.dto

import de.elegantsoftware.blitzpay.gateways.api.GatewayType
import java.math.BigDecimal
import java.util.UUID
import kotlin.time.Instant

data class PaymentLinkDto(
    val id: UUID,
    val merchantId: UUID,
    val productIds: List<UUID>,
    val amount: BigDecimal,
    val currency: String,
    val description: String,
    val gatewayType: GatewayType,
    val paymentUrl: String,
    val qrCodeUrl: String?,
    val expiresAt: Instant,
    val isActive: Boolean,
    val createdAt: Instant
)