package de.elegantsoftware.blitzpay.gateways.outbound.truelayer.model

import org.springframework.data.repository.query.Param

data class TrueLayerAuthResponse(
    @Param("access_token")
    val accessToken: String,
    
    @Param("token_type")
    val tokenType: String,
    
    @Param("expires_in")
    val expiresIn: Int,
    
    @Param("refresh_token")
    val refreshToken: String? = null,
    
    val scope: String
)