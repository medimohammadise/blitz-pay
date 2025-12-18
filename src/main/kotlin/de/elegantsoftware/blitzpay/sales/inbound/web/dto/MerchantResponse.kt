package de.elegantsoftware.blitzpay.sales.inbound.web.dto

import java.util.UUID

data class MerchantResponse(
    val id: UUID,
    val name: String,
    val email: String
)