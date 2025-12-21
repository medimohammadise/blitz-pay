package de.elegantsoftware.blitzpay.invoice.inbound.web.dto

import java.math.BigDecimal
import java.time.LocalDate

data class PaymentWebRequest(
    val amount: BigDecimal,
    val paymentDate: LocalDate = LocalDate.now(),
    val paymentMethod: String,
    val reference: String? = null,
    val transactionId: String? = null
)