package de.elegantsoftware.blitzpay.invoice.domain.events

import de.elegantsoftware.blitzpay.invoice.domain.Invoice
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

sealed class InvoiceEvent(
    open val aggregateId: UUID,
    open val eventType: String,
    open val timestamp: LocalDateTime = LocalDateTime.now(),
    open val version: Int = 1
)

data class InvoiceCreatedEvent(
    val invoiceId: UUID,
    val invoiceNumber: String,
    val merchantId: Long,
    val customerId: Long?,
    val totalAmount: String,
    val currency: String,
    val status: String,
    override val aggregateId: UUID = invoiceId,
    override val eventType: String = "INVOICE_CREATED",
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val version: Int = 1
) : InvoiceEvent(aggregateId, eventType, timestamp, version)

data class InvoiceIssuedEvent(
    val invoiceId: UUID,
    val invoiceNumber: String,
    val merchantId: Long,
    val customerId: Long?,
    val issueDate: LocalDate,
    val dueDate: LocalDate,
    val totalAmount: String,
    override val aggregateId: UUID = invoiceId,
    override val eventType: String = "INVOICE_ISSUED",
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val version: Int = 1
) : InvoiceEvent(aggregateId, eventType, timestamp, version)

data class InvoiceSentEvent(
    val invoiceId: UUID,
    val invoiceNumber: String,
    val merchantId: Long,
    val customerId: Long?,
    val customerEmail: String?,
    val sentDate: LocalDate,
    override val aggregateId: UUID = invoiceId,
    override val eventType: String = "INVOICE_SENT",
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val version: Int = 1
) : InvoiceEvent(aggregateId, eventType, timestamp, version)

data class InvoicePaidEvent(
    val invoiceId: UUID,
    val invoiceNumber: String,
    val merchantId: Long,
    val customerId: Long?,
    val amountPaid: String,
    val paymentMethod: String,
    val paymentReference: String?,
    val paymentDate: LocalDate,
    override val aggregateId: UUID = invoiceId,
    override val eventType: String = "INVOICE_PAID",
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val version: Int = 1
) : InvoiceEvent(aggregateId, eventType, timestamp, version)

data class InvoicePartiallyPaidEvent(
    val invoiceId: UUID,
    val invoiceNumber: String,
    val merchantId: Long,
    val customerId: Long?,
    val amountPaid: String,
    val remainingBalance: String,
    val paymentDate: LocalDate,
    override val aggregateId: UUID = invoiceId,
    override val eventType: String = "INVOICE_PARTIALLY_PAID",
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val version: Int = 1
) : InvoiceEvent(aggregateId, eventType, timestamp, version)

data class InvoiceCancelledEvent(
    val invoiceId: UUID,
    val invoiceNumber: String,
    val merchantId: Long,
    val reason: String,
    override val aggregateId: UUID = invoiceId,
    override val eventType: String = "INVOICE_CANCELLED",
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val version: Int = 1
) : InvoiceEvent(aggregateId, eventType, timestamp, version)

data class InvoiceOverdueEvent(
    val invoiceId: UUID,
    val invoiceNumber: String,
    val merchantId: Long,
    val customerId: Long?,
    val dueDate: LocalDate,
    val overdueDays: Int,
    val totalAmount: String,
    override val aggregateId: UUID = invoiceId,
    override val eventType: String = "INVOICE_OVERDUE",
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val version: Int = 1
) : InvoiceEvent(aggregateId, eventType, timestamp, version)

data class InvoiceQRCodeGeneratedEvent(
    val invoiceId: UUID,
    val invoiceNumber: String,
    val merchantId: Long,
    val amount: String,
    val currency: String,
    val qrCodeType: String,
    override val aggregateId: UUID = invoiceId,
    override val eventType: String = "INVOICE_QR_CODE_GENERATED",
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val version: Int = 1
) : InvoiceEvent(aggregateId, eventType, timestamp, version)

data class InvoiceItemAddedEvent(
    val invoiceId: UUID,
    val invoiceNumber: String,
    val productId: Long,
    val productUuid: UUID,
    val productName: String,
    val quantity: String,
    val unitPrice: String,
    val lineTotal: String,
    override val aggregateId: UUID = invoiceId,
    override val eventType: String = "INVOICE_ITEM_ADDED",
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val version: Int = 1
) : InvoiceEvent(aggregateId, eventType, timestamp, version)

data class InvoiceItemRemovedEvent(
    val invoiceId: UUID,
    val invoiceNumber: String,
    val productId: Long,
    val productUuid: UUID,
    val productName: String,
    val quantity: String,
    val lineTotal: String,
    override val aggregateId: UUID = invoiceId,
    override val eventType: String = "INVOICE_ITEM_REMOVED",
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val version: Int = 1
) : InvoiceEvent(aggregateId, eventType, timestamp, version)