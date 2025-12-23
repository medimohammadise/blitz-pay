package de.elegantsoftware.blitzpay.gateways.outbound.truelayer.model

import org.springframework.data.repository.query.Param

data class TrueLayerPaymentStatusResponse(
    val id: String,
    val status: String,
    
    @Param("failure_reason")
    val failureReason: String?,
    
    @Param("settled_at")
    val settledAt: String?
)