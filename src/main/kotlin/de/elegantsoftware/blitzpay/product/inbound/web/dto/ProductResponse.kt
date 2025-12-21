package de.elegantsoftware.blitzpay.product.inbound.web.dto

import de.elegantsoftware.blitzpay.product.domain.ProductStatus
import de.elegantsoftware.blitzpay.product.domain.ProductType
import java.util.UUID
import kotlin.time.Instant

data class ProductResponse(
    val publicId: UUID,
    val merchantId: Long,
    val name: String,
    val description: String,
    val type: ProductType,
    val status: ProductStatus,
    val basePrice: PriceResponse,
    val variants: List<ProductVariantResponse>,
    val inventory: ProductInventoryResponse,
    val categoryId: Long?,
    val tags: Set<String>,
    val metadata: Map<String, String>,
    val createdAt: Instant,
    val updatedAt: Instant
)