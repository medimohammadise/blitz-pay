package de.elegantsoftware.blitzpay.invoice.inbound.web.dto

import kotlin.time.Instant


data class InvoiceMetadataWebResponse(
    val createdAt: Instant,
    val updatedAt: Instant,
    val sentDate: Instant?,
    val paidDate: Instant?,
    val overdueDate: Instant?,
    val reminderSentDate: Instant?,
    val qrCodeGenerated: Boolean,
    val qrCodeGeneratedAt: Instant?
)