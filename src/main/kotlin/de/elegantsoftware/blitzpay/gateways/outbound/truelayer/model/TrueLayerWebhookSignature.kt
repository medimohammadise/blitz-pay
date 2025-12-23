package de.elegantsoftware.blitzpay.gateways.outbound.truelayer.model

data class TrueLayerWebhookSignature(
    val signature: String,
    val timestamp: String,
    val body: String
)