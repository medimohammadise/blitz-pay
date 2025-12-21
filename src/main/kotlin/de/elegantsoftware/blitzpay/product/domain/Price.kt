package de.elegantsoftware.blitzpay.product.domain

import java.math.BigDecimal

data class Price(
    val amount: BigDecimal,
    val currency: String,
    val taxInclusive: Boolean = false
){
    init {
        require(amount >= BigDecimal.ZERO) { "Price amount must be non-negative" }
        require(currency.isNotBlank()) { "Currency must not be blank" }
    }
}