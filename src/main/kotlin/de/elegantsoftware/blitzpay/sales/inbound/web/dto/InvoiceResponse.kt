package de.elegantsoftware.blitzpay.sales.inbound.web.dto

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class InvoiceResponse(
    val id: UUID,
    val invoiceNumber: String,
    val merchant: MerchantResponse,
    val items: List<InvoiceItemResponse>,
    val status: String,
    val issueDate: LocalDate?,
    val dueDate: LocalDate,
    val billingAddress: BillingAddressResponse?,
    val notes: String?,
    val totals: InvoiceTotalsResponse,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class BillingAddressResponse(
    val street: String,
    val city: String,
    val postalCode: String,
    val country: String
)

data class InvoiceTotalsResponse(
    val subtotal: BigDecimal,
    val tax: BigDecimal,
    val total: BigDecimal,
    val paid: BigDecimal,
    val currency: String,
    val paymentDate: LocalDate?,
    val isPaid: Boolean,
    val isOverdue: Boolean
)