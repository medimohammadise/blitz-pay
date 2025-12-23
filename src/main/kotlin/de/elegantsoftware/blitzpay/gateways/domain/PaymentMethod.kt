package de.elegantsoftware.blitzpay.gateways.domain

import de.elegantsoftware.blitzpay.gateways.api.GatewayType

data class PaymentMethod(
    val type: GatewayType,
    val isDefault: Boolean = false,
    val isEnabled: Boolean = true
)