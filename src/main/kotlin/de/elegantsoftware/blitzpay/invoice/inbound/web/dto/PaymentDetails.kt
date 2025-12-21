package de.elegantsoftware.blitzpay.invoice.inbound.web.dto

import de.elegantsoftware.blitzpay.invoice.domain.PaymentMethodType
import java.math.BigDecimal
import java.time.LocalDate

data class PaymentDetails(
    val amount: BigDecimal,
    val paymentDate: LocalDate = LocalDate.now(),
    val paymentMethod: PaymentMethodType,
    val reference: String? = null,
    val transactionId: String? = null
)