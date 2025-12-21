package de.elegantsoftware.blitzpay.product.inbound.web.dto

import java.math.BigDecimal

data class PriceResponse(
    val amount: BigDecimal,
    val currency: String,
    val taxInclusive: Boolean
)