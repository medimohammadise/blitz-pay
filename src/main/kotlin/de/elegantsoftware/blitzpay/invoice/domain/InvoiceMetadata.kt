package de.elegantsoftware.blitzpay.invoice.domain

import kotlin.time.Clock
import kotlin.time.Instant

data class InvoiceMetadata(
    val createdAt: Instant = Clock.System.now(),
    var updatedAt: Instant = Clock.System.now(),
    var sentDate: Instant? = null,
    var paidDate: Instant? = null,
    var overdueDate: Instant? = null,
    var reminderSentDate: Instant? = null,
    var qrCodeGenerated: Boolean = false,
    var qrCodeGeneratedAt: Instant? = null,
    val customFields: MutableMap<String, String> = mutableMapOf()
)