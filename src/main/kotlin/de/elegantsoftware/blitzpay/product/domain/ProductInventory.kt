package de.elegantsoftware.blitzpay.product.domain

data class ProductInventory(
    val quantity: Int = 0,
    val lowStockThreshold: Int = 10,
    val isTracked: Boolean = true
){
    init {
        require(quantity >= 0) { "Quantity must be non-negative" }
        require(lowStockThreshold >= 0) { "Low stock threshold must be non-negative" }
    }
}