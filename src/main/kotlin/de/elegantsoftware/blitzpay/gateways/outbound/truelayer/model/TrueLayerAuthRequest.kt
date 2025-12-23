package de.elegantsoftware.blitzpay.gateways.outbound.truelayer.model

import org.springframework.data.repository.query.Param

data class TrueLayerAuthRequest(
    @Param("client_id")
    val clientId: String,
    
    @Param("client_secret")
    val clientSecret: String,
    
    @Param("grant_type")
    val grantType: String = "client_credentials",
    
    val scope: String = "payments"
)