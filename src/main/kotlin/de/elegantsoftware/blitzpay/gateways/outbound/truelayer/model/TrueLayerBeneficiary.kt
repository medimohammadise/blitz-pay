package de.elegantsoftware.blitzpay.gateways.outbound.truelayer.model

import org.springframework.data.repository.query.Param

data class TrueLayerBeneficiary(
    val type: String = "merchant_account",
    
    @Param("merchant_account_id")
    val merchantAccountId: String
)