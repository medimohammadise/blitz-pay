package de.elegantsoftware.blitzpay.invoice.inbound.web.dto

import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Instant

data class CreateInvoiceWebRequest(
    val merchantId: Long,
    val merchantUuid: UUID,
    val merchantName: String,
    val merchantAddress: AddressWebRequest,
    val customerId: Long? = null,
    val customerUuid: UUID? = null,
    val customerName: String,
    val customerEmail: String? = null,
    val customerAddress: AddressWebRequest? = null,
    val invoiceType: String = "STANDARD",
    val issueDate: Instant = Clock.System.now(),
    val dueDate: Instant? = null,
    val paymentTerm: String = "NET_30",
    val currency: String = "EUR",
    val notes: String? = null,
    val termsAndConditions: String? = null,
    val paymentMethods: List<PaymentMethodWebRequest> = emptyList(),
    val items: List<CreateInvoiceItemWebRequest> = emptyList()
)