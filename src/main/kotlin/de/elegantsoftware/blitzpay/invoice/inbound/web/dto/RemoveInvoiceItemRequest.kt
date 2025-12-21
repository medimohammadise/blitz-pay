package de.elegantsoftware.blitzpay.invoice.inbound.web.dto

data class RemoveInvoiceItemRequest(
    val invoiceId: Long,
    val productId: Long
)