package de.elegantsoftware.blitzpay.invoice.domain

import java.math.BigDecimal
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Instant

data class Invoice(
    val id: InvoiceId,
    val uuid: UUID = UUID.randomUUID(),
    val merchantId: Long,
    val merchantUuid: UUID,
    val merchantName: String,
    val merchantTaxId: String?,
    val merchantAddress: Address,
    val customerId: Long?,
    val customerUuid: UUID?,
    val customerName: String,
    val customerEmail: String?,
    var customerAddress: Address?,
    val customerTaxId: String?,
    val invoiceNumber: String,
    val invoiceType: InvoiceType = InvoiceType.STANDARD,
    val issueDate: Instant,
    val dueDate: Instant,
    val paymentTerm: PaymentTerm,
    var status: InvoiceStatus,
    val items: MutableList<InvoiceItem> = mutableListOf(),
    var subtotal: BigDecimal,
    var taxAmount: BigDecimal,
    var totalAmount: BigDecimal,
    val currency: String = "EUR",
    var notes: String? = null,
    var termsAndConditions: String? = null,
    var paymentMethods: List<PaymentMethod> = emptyList(),
    val metadata: InvoiceMetadata = InvoiceMetadata(),
    val version: Long = 0
) {
    fun addItem(item: InvoiceItem): Invoice {
        items.add(item)
        recalculateTotals()
        return this
    }

    fun removeItem(productId: Long): Invoice {
        items.removeIf { it.productId == productId }
        recalculateTotals()
        return this
    }

    fun issue(): Invoice {
        require(status == InvoiceStatus.DRAFT) { "Only DRAFT invoices can be issued" }
        status = InvoiceStatus.ISSUED
        return this
    }

    fun send(): Invoice {
        require(status == InvoiceStatus.ISSUED) { "Only ISSUED invoices can be sent" }
        status = InvoiceStatus.SENT
        metadata.sentDate = Clock.System.now()
        return this
    }

    fun markAsPaid(paymentDate: Instant = Clock.System.now()): Invoice {
        status = InvoiceStatus.PAID
        metadata.paidDate = paymentDate
        return this
    }

    fun markAsPartiallyPaid(): Invoice {
        status = InvoiceStatus.PARTIALLY_PAID
        return this
    }

    fun cancel(reason: String): Invoice {
        require(status != InvoiceStatus.PAID) { "Cannot cancel a PAID invoice" }
        status = InvoiceStatus.CANCELLED
        metadata.customFields["cancellationReason"] = reason
        return this
    }

    fun addPaymentMethod(method: PaymentMethod): Invoice {
        return this.copy(paymentMethods = paymentMethods + method)
    }

    private fun recalculateTotals() {
        subtotal = items.sumOf { it.lineTotal }
        taxAmount = items.sumOf { it.taxAmount }
        totalAmount = subtotal + taxAmount
    }

    companion object {
        fun create(
            merchantId: Long,
            merchantUuid: UUID,
            merchantName: String,
            merchantAddress: Address,
            customerId: Long?,
            customerUuid: UUID?,
            customerName: String,
            customerEmail: String?,
            customerAddress: Address?,
            invoiceNumber: String,
            issueDate: Instant,
            dueDate: Instant,
            paymentTerm: PaymentTerm
        ): Invoice {
            return Invoice(
                id = InvoiceId(0),
                merchantId = merchantId,
                merchantUuid = merchantUuid,
                merchantName = merchantName,
                merchantAddress = merchantAddress,
                merchantTaxId = null,
                customerId = customerId,
                customerUuid = customerUuid,
                customerName = customerName,
                customerEmail = customerEmail,
                customerAddress = customerAddress,
                customerTaxId = null,
                invoiceNumber = invoiceNumber,
                issueDate = issueDate,
                dueDate = dueDate,
                paymentTerm = paymentTerm,
                status = InvoiceStatus.DRAFT,
                subtotal = BigDecimal.ZERO,
                taxAmount = BigDecimal.ZERO,
                totalAmount = BigDecimal.ZERO
            )
        }
    }
}