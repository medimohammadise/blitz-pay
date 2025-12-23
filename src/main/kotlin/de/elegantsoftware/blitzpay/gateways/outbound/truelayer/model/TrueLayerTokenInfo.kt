package de.elegantsoftware.blitzpay.gateways.outbound.truelayer.model

import kotlin.time.Instant

data class TrueLayerTokenInfo(
    val accessToken: String,
    val tokenType: String,
    val expiresAt: Instant, // Unix timestamp
    val refreshToken: String? = null,
    val scope: String
)