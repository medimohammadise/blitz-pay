package de.elegantsoftware.blitzpay.invoice.inbound.web.dto

import java.time.LocalDate
import java.util.UUID

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
    val issueDate: LocalDate = LocalDate.now(),
    val dueDate: LocalDate? = null,
    val paymentTerm: String = "NET_30",
    val currency: String = "EUR",
    val notes: String? = null,
    val termsAndConditions: String? = null,
    val paymentMethods: List<PaymentMethodWebRequest> = emptyList(),
    val items: List<CreateInvoiceItemWebRequest> = emptyList()
)