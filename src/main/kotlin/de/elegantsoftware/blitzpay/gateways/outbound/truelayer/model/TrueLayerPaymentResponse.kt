package de.elegantsoftware.blitzpay.gateways.outbound.truelayer.model

import org.springframework.data.repository.query.Param

data class TrueLayerPaymentResponse(
    val id: String,
    
    @Param("authorization_url")
    val authorizationUrl: String?,
    
    @Param("qr_code_url")
    val qrCodeUrl: String?,
    
    val status: String,
    
    @Param("created_at")
    val createdAt: String,
    
    @Param("expires_at")
    val expiresAt: String,
    
    val metadata: Map<String, String> = emptyMap()
)