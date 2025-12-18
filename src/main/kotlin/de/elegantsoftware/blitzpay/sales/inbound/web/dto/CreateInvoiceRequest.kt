package de.elegantsoftware.blitzpay.sales.inbound.web.dto

import jakarta.validation.constraints.*
import java.time.LocalDate
import java.util.UUID

data class CreateInvoiceRequest(
    @field:NotNull
    val merchantId: UUID,

    val merchantName: String? = null,
    val merchantEmail: String? = null,

    @field:Future
    val dueDate: LocalDate? = null,

    val billingAddress: BillingAddressRequest? = null,
    val notes: String? = null
)