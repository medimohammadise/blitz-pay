package de.elegantsoftware.blitzpay.invoice.inbound.web.dto

data class UpdateInvoiceWebRequest(
    val invoiceId: Long,
    val notes: String? = null,
    val termsAndConditions: String? = null,
    val customerAddress: AddressWebRequest? = null,
    val paymentMethods: List<PaymentMethodWebRequest>? = null
)