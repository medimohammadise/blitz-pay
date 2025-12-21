package de.elegantsoftware.blitzpay.invoice.inbound.web.dto

import java.math.BigDecimal

data class TaxSummaryWebResponse(
    val taxableAmount: BigDecimal,
    val taxAmount: BigDecimal,
    val taxRates: Map<BigDecimal, BigDecimal>
)