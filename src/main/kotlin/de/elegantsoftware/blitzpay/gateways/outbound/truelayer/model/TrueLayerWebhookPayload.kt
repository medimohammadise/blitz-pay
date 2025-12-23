package de.elegantsoftware.blitzpay.gateways.outbound.truelayer.model

import org.springframework.data.repository.query.Param

data class TrueLayerWebhookPayload(
    @Param("event_id")
    val eventId: String,
    
    @Param("event_type")
    val eventType: String,
    
    @Param("payment_id")
    val paymentId: String,
    
    val status: String,
    
    @Param("resource_type")
    val resourceType: String,
    
    @Param("timestamp")
    val timestamp: String,
    
    @Param("failure_reason")
    val failureReason: String? = null
)