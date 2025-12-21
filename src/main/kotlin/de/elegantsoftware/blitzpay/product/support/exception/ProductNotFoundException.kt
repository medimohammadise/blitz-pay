package de.elegantsoftware.blitzpay.product.support.exception

import de.elegantsoftware.blitzpay.product.domain.ProductId
import java.util.UUID

class ProductNotFoundException(
    productId: ProductId? = null,
    sku: String? = null,
    publicId: UUID? = null,
    merchantId: Long? = null
) : ProductException(
    errorCode = ProductErrorCode.PRODUCT_NOT_FOUND,
    message = when {
        productId != null -> "Product with ID $productId not found"
        sku != null && merchantId != null -> "Product with SKU '$sku' not found for merchant ID $merchantId"
        publicId != null && merchantId != null -> "Product with public ID $publicId not found for merchant ID $merchantId"
        publicId != null -> "Product with public ID $publicId not found"
        else -> "Product not found"
    }
)