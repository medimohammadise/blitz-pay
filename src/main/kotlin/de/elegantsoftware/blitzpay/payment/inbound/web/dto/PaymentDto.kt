package de.elegantsoftware.blitzpay.payment.inbound.web.dto

import de.elegantsoftware.blitzpay.gateways.api.PaymentStatus
import de.elegantsoftware.blitzpay.gateways.api.GatewayType
import java.math.BigDecimal
import java.util.UUID
import kotlin.time.Instant

data class PaymentDto(
    val id: UUID,
    val merchantId: UUID,
    val productIds: List<UUID>,
    val amount: BigDecimal,
    val currency: String,
    val gatewayType: GatewayType,
    val gatewayPaymentId: String?,
    val status: PaymentStatus,
    val customerEmail: String?,
    val description: String,
    val createdAt: Instant,
    val paidAt: Instant?,
    val paymentLinkId: UUID?
)