package de.elegantsoftware.blitzpay.product.support.exception

import de.elegantsoftware.blitzpay.product.domain.ProductId
import java.util.UUID

class ProductInactiveException(
    productId: ProductId? = null,
    publicId: UUID? = null
) : ProductException(
    errorCode = ProductErrorCode.PRODUCT_INACTIVE,
    message = when {
        productId != null -> "Product with ID $productId is inactive"
        publicId != null -> "Product with public ID $publicId is inactive"
        else -> "Product is inactive"
    }
)