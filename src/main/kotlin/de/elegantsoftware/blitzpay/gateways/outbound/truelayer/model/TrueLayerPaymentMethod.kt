package de.elegantsoftware.blitzpay.gateways.outbound.truelayer.model

data class TrueLayerPaymentMethod(
    val type: String = "bank_transfer"
)