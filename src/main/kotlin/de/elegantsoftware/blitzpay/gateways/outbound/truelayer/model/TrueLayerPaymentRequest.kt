package de.elegantsoftware.blitzpay.gateways.outbound.truelayer.model

import org.springframework.data.repository.query.Param

data class TrueLayerPaymentRequest(
    @Param("amount_in_minor")
    val amountInMinor: Int,
    
    val currency: String,
    
    @Param("payment_method")
    val paymentMethod: TrueLayerPaymentMethod,
    
    val beneficiary: TrueLayerBeneficiary,
    val metadata: Map<String, String> = emptyMap()
)