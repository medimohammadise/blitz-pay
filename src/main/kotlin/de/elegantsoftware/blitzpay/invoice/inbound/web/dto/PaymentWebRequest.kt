package de.elegantsoftware.blitzpay.invoice.inbound.web.dto

import java.math.BigDecimal
import kotlin.time.Clock
import kotlin.time.Instant

data class PaymentWebRequest(
    val amount: BigDecimal,
    val paymentDate: Instant = Clock.System.now(),
    val paymentMethod: String,
    val reference: String? = null,
    val transactionId: String? = null
)