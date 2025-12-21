package de.elegantsoftware.blitzpay.product.support.exception

class ProductValidationException(
    field: String? = null,
    message: String = "Product validation failed"
) : ProductException(
    errorCode = ProductErrorCode.PRODUCT_VALIDATION_FAILED,
    message = when {
        field != null -> "Validation failed for field '$field': $message"
        else -> message
    }
)