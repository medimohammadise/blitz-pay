package de.elegantsoftware.blitzpay.invoice.api

import de.elegantsoftware.blitzpay.invoice.domain.*
import java.math.BigDecimal
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Instant

interface InvoiceService {
    fun createInvoice(request: CreateInvoiceApiRequest): Invoice
    fun updateInvoice(request: UpdateInvoiceApiRequest): Invoice
    fun addItem(request: AddInvoiceItemApiRequest): Invoice
    fun removeItem(request: RemoveInvoiceItemApiRequest): Invoice
    fun issueInvoice(invoiceId: Long): Invoice
    fun sendInvoice(invoiceId: Long): Invoice
    fun markAsPaid(invoiceId: Long, paymentDetails: PaymentDetailsApi): Invoice
    fun markAsPartiallyPaid(invoiceId: Long, amountPaid: BigDecimal): Invoice
    fun cancelInvoice(invoiceId: Long, reason: String): Invoice
    fun generateQRCode(invoiceId: Long): String

    fun getInvoiceById(id: Long): Invoice?
    fun getInvoiceByUuid(uuid: UUID): Invoice?
    fun getInvoicesByMerchant(merchantId: Long, page: Int, size: Int): List<Invoice>
    fun getInvoicesByCustomer(customerId: Long, merchantId: Long): List<Invoice>
    fun findOverdueInvoices(merchantId: Long? = null): List<Invoice>
    fun generateInvoiceNumber(merchantId: Long): String

    fun calculateTaxSummary(invoiceId: Long): TaxSummaryApi
    fun generateInvoicePdf(invoiceId: Long): ByteArray
}

// API-level DTOs (used by the service interface)
data class CreateInvoiceApiRequest(
    val merchantId: Long,
    val merchantUuid: UUID,
    val merchantName: String,
    val merchantAddress: AddressApi,
    val customerId: Long? = null,
    val customerUuid: UUID? = null,
    val customerName: String,
    val customerEmail: String? = null,
    val customerAddress: AddressApi? = null,
    val invoiceType: InvoiceType = InvoiceType.STANDARD,
    val issueDate: Instant = Clock.System.now(),
    val dueDate: Instant? = null,
    val paymentTerm: PaymentTerm = PaymentTerm.NET_30,
    val currency: String = "EUR",
    val notes: String? = null,
    val termsAndConditions: String? = null,
    val paymentMethods: List<PaymentMethodApi> = emptyList(),
    val items: List<CreateInvoiceItemApiRequest> = emptyList()
)

data class CreateInvoiceItemApiRequest(
    val productId: Long,
    val productUuid: UUID,
    val merchantProductId: Long,
    val productName: String,
    val productSku: String,
    val description: String? = null,
    val quantity: BigDecimal,
    val unitPrice: BigDecimal,
    val taxRate: BigDecimal = BigDecimal.ZERO,
    val discountPercentage: BigDecimal = BigDecimal.ZERO
)

data class UpdateInvoiceApiRequest(
    val invoiceId: Long,
    val notes: String? = null,
    val termsAndConditions: String? = null,
    val customerAddress: AddressApi? = null,
    val paymentMethods: List<PaymentMethodApi>? = null
)

data class AddInvoiceItemApiRequest(
    val invoiceId: Long,
    val productId: Long,
    val productUuid: UUID,
    val merchantProductId: Long,
    val productName: String,
    val productSku: String,
    val description: String? = null,
    val quantity: BigDecimal,
    val unitPrice: BigDecimal,
    val taxRate: BigDecimal = BigDecimal.ZERO,
    val discountPercentage: BigDecimal = BigDecimal.ZERO
)

data class RemoveInvoiceItemApiRequest(
    val invoiceId: Long,
    val productId: Long
)

data class PaymentDetailsApi(
    val amount: BigDecimal,
    val paymentDate: Instant = Clock.System.now(),
    val paymentMethod: PaymentMethodType,
    val reference: String? = null,
    val transactionId: String? = null
)

data class TaxSummaryApi(
    val taxableAmount: BigDecimal,
    val taxAmount: BigDecimal,
    val taxRates: Map<BigDecimal, BigDecimal>
)

// Reuse domain enums
data class AddressApi(
    val street: String,
    val city: String,
    val state: String?,
    val postalCode: String,
    val country: String,
    val phone: String? = null,
    val email: String? = null
)

data class PaymentMethodApi(
    val type: PaymentMethodType,
    val details: String? = null,
    val isEnabled: Boolean = true
)