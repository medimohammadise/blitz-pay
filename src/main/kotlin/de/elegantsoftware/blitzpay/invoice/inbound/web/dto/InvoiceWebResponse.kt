package de.elegantsoftware.blitzpay.invoice.inbound.web.dto

import java.math.BigDecimal
import java.util.UUID
import kotlin.time.Instant

data class InvoiceWebResponse(
    val id: Long,
    val uuid: UUID,
    val merchantId: Long,
    val merchantUuid: UUID,
    val merchantName: String,
    val merchantAddress: AddressWebResponse,
    val customerId: Long?,
    val customerUuid: UUID?,
    val customerName: String,
    val customerEmail: String?,
    val customerAddress: AddressWebResponse?,
    val invoiceNumber: String,
    val invoiceType: String,
    val issueDate: Instant,
    val dueDate: Instant,
    val paymentTerm: String,
    val status: String,
    val items: List<InvoiceItemWebResponse>,
    val subtotal: BigDecimal,
    val taxAmount: BigDecimal,
    val totalAmount: BigDecimal,
    val currency: String,
    val notes: String?,
    val termsAndConditions: String?,
    val paymentMethods: List<PaymentMethodWebResponse>,
    val metadata: InvoiceMetadataWebResponse,
    val version: Long
)