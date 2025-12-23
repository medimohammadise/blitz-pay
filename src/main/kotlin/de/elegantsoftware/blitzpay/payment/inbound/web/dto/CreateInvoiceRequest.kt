package de.elegantsoftware.blitzpay.payment.inbound.web.dto

import java.util.UUID

data class CreateInvoiceRequest(
    val merchantId: UUID,
    val productIds: List<UUID>,
    val customerEmail: String,
    val description: String = ""
)