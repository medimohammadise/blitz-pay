package de.elegantsoftware.blitzpay.product.domain

import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Instant

data class Product(
    val id: ProductId,
    val publicId: UUID,
    val merchantId: Long,
    val name: String,
    val description: String,
    val type: ProductType,
    val status: ProductStatus,
    val basePrice: Price,
    val variants: List<ProductVariant> = emptyList(),
    val inventory: ProductInventory = ProductInventory(),
    val categoryId: Long? = null,
    val tags: Set<String> = emptySet(),
    val metadata: Map<String, String> = emptyMap(),
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now(),
) {
    init {
        require(name.isNotBlank()) { "Product name must not be blank" }
        require(description.isNotBlank()) { "Product description must not be blank" }
    }

    fun hasSufficientStock(quantity: Int): Boolean {
        return !inventory.isTracked || inventory.quantity >= quantity
    }

    fun reduceStock(quantity: Int): Product {
        require(hasSufficientStock(quantity)) { "Insufficient stock" }
        return copy(
            inventory = inventory.copy(quantity = inventory.quantity - quantity),
            updatedAt = Clock.System.now(),
        )
    }

    fun increaseStock(quantity: Int): Product {
        require(quantity > 0) { "Quantity must be positive" }
        return copy(
            inventory = inventory.copy(quantity = inventory.quantity + quantity),
            updatedAt = Clock.System.now(),
        )
    }
}