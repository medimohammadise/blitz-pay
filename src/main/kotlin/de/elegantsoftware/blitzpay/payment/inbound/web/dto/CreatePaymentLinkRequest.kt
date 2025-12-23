package de.elegantsoftware.blitzpay.payment.inbound.web.dto

import de.elegantsoftware.blitzpay.gateways.api.GatewayType
import java.util.UUID

data class CreatePaymentLinkRequest(
    val merchantId: UUID,
    val productIds: List<UUID>,
    val gatewayType: GatewayType = GatewayType.TRUELAYER,
    val description: String = ""
)