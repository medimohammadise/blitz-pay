package de.elegantsoftware.blitzpay.product.domain.events

import de.elegantsoftware.blitzpay.product.domain.Product
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Instant

data class ProductUpdated(
    val productId: UUID,
    val merchantId: Long,
    val updatedFields: Set<String>,
    val updatedAt: Instant = Clock.System.now()
) {
    companion object {
        fun from(product: Product, updatedFields: Set<String>): ProductUpdated {
            return ProductUpdated(
                productId = product.publicId,
                merchantId = product.merchantId,
                updatedFields = updatedFields,
                updatedAt = product.updatedAt
            )
        }
    }
}