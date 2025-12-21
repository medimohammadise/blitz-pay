package de.elegantsoftware.blitzpay.invoice.domain.events

import java.math.BigDecimal
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Instant

data class InvoiceItemAddedEvent(
    val invoiceId: Long,
    val invoiceUuid: UUID,
    val productId: Long,
    val productUuid: UUID,
    val productName: String,
    val quantity: BigDecimal,
    val unitPrice: BigDecimal,
    val lineTotal: BigDecimal,
    val timestamp: Instant = Clock.System.now(),
    val eventId: UUID = UUID.randomUUID()
)