package de.elegantsoftware.blitzpay.product.support.exception

open class ProductException(
    val errorCode: ProductErrorCode,
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)