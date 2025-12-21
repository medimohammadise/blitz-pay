package de.elegantsoftware.blitzpay.product.inbound.web.dto

data class ProductInventoryResponse(
    val quantity: Int,
    val lowStockThreshold: Int,
    val isTracked: Boolean
)