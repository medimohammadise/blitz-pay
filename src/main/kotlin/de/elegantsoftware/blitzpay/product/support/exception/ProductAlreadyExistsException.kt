package de.elegantsoftware.blitzpay.product.support.exception

class ProductAlreadyExistsException(
    sku: String,
    merchantId: Long
) : ProductException(
    errorCode = ProductErrorCode.PRODUCT_ALREADY_EXISTS,
    message = "Product with SKU '$sku' already exists for merchant ID $merchantId"
)