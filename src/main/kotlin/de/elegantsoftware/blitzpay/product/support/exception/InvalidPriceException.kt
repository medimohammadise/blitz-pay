package de.elegantsoftware.blitzpay.product.support.exception

import java.math.BigDecimal

class InvalidPriceException(
    price: BigDecimal? = null,
    field: String? = null,
    message: String = "Invalid price"
) : ProductException(
    errorCode = ProductErrorCode.INVALID_PRICE,
    message = when {
        price != null && field != null -> "Invalid price for $field: $price"
        price != null -> "Invalid price: $price"
        field != null -> "Invalid price for $field"
        else -> message
    }
)