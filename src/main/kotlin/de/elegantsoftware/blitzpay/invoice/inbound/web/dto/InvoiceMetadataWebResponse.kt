package de.elegantsoftware.blitzpay.invoice.inbound.web.dto

import java.time.LocalDate
import java.time.LocalDateTime

data class InvoiceMetadataWebResponse(
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val sentDate: LocalDate?,
    val paidDate: LocalDate?,
    val overdueDate: LocalDate?,
    val reminderSentDate: LocalDate?,
    val qrCodeGenerated: Boolean,
    val qrCodeGeneratedAt: LocalDateTime?
)