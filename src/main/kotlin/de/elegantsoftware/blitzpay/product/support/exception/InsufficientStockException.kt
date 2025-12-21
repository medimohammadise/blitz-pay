package de.elegantsoftware.blitzpay.product.support.exception

import java.util.UUID

class InsufficientStockException(
    productPublicId: UUID,
    requestedQuantity: Int,
    availableQuantity: Int
) : ProductException(
    errorCode = ProductErrorCode.INSUFFICIENT_STOCK,
    message = "Insufficient stock for product $productPublicId. Requested: $requestedQuantity, Available: $availableQuantity"
)