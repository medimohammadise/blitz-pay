package de.elegantsoftware.blitzpay.product.inbound.web.dto

import de.elegantsoftware.blitzpay.product.domain.ProductType
import jakarta.validation.constraints.PositiveOrZero
import java.math.BigDecimal

data class UpdateProductRequest(
    val name: String? = null,
    val description: String? = null,
    val type: ProductType? = null,
    @field:PositiveOrZero
    val basePrice: BigDecimal? = null,
    val currency: String? = null,
    val variants: List<ProductVariantRequest>? = null,
    val categoryId: Long? = null,
    val tags: Set<String>? = null,
    val metadata: Map<String, String>? = null
)