package de.elegantsoftware.blitzpay.sales.inbound.web.dto

import jakarta.validation.constraints.PastOrPresent
import java.time.LocalDate

data class MarkInvoicePaidRequest(
    @field:PastOrPresent
    val paymentDate: LocalDate? = null,

    val paymentMethod: String? = null,
    val transactionId: String? = null
)