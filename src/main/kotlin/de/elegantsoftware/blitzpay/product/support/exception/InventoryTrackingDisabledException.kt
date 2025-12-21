package de.elegantsoftware.blitzpay.product.support.exception

import java.util.UUID

class InventoryTrackingDisabledException(
    productPublicId: UUID
) : ProductException(
    errorCode = ProductErrorCode.INVENTORY_TRACKING_DISABLED,
    message = "Inventory tracking is disabled for product $productPublicId"
)