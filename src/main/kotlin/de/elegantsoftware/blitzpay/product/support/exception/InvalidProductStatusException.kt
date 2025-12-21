package de.elegantsoftware.blitzpay.product.support.exception

import de.elegantsoftware.blitzpay.product.domain.ProductStatus

class InvalidProductStatusException(
    currentStatus: ProductStatus,
    expectedStatus: ProductStatus? = null,
    validStatuses: Set<ProductStatus>? = null
) : ProductException(
    errorCode = ProductErrorCode.INVALID_PRODUCT_STATUS,
    message = when {
        expectedStatus != null -> "Product status is $currentStatus but expected $expectedStatus"
        validStatuses != null -> "Product status is $currentStatus but must be one of: $validStatuses"
        else -> "Product status $currentStatus is invalid for this operation"
    }
)