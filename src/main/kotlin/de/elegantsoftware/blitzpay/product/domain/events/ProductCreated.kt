package de.elegantsoftware.blitzpay.product.domain.events

import de.elegantsoftware.blitzpay.product.domain.Product
import java.util.*
import kotlin.time.Clock
import kotlin.time.Instant

data class ProductCreated(
    val productId: UUID,
    val merchantId: Long,
    val name: String,
    val type: String,
    val createdAt: Instant = Clock.System.now()
) {
    companion object {
        fun from(product: Product): ProductCreated {
            return ProductCreated(
                productId = product.publicId,
                merchantId = product.merchantId,
                name = product.name,
                type = product.type.name,
                createdAt = product.createdAt
            )
        }
    }
}