package de.elegantsoftware.blitzpay.product.domain

import java.util.UUID

data class ProductVariant(
    val id: UUID = UUID.randomUUID(),
    val sku: String,
    val name: String,
    val price: Price,
    val attributes: Map<String, String> = emptyMap()
){
    init {
        require(sku.isNotBlank()) { "SKU must not be blank" }
        require(name.isNotBlank()) { "Name must not be blank" }
    }
}