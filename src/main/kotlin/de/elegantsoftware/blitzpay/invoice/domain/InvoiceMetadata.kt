package de.elegantsoftware.blitzpay.invoice.domain

import java.time.LocalDate
import java.time.LocalDateTime

data class InvoiceMetadata(
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    var sentDate: LocalDate? = null,
    var paidDate: LocalDate? = null,
    var overdueDate: LocalDate? = null,
    var reminderSentDate: LocalDate? = null,
    var qrCodeGenerated: Boolean = false,
    var qrCodeGeneratedAt: LocalDateTime? = null,
    val customFields: MutableMap<String, String> = mutableMapOf()
)