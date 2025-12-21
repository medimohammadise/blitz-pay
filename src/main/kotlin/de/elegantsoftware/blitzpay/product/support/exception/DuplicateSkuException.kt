package de.elegantsoftware.blitzpay.product.support.exception

class DuplicateSkuException(
    sku: String,
    merchantId: Long
) : ProductException(
    errorCode = ProductErrorCode.DUPLICATE_SKU,
    message = "SKU '$sku' already exists for merchant ID $merchantId"
)