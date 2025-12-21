package de.elegantsoftware.blitzpay.product.inbound.web.dto

import de.elegantsoftware.blitzpay.product.domain.ProductType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero
import java.math.BigDecimal

data class CreateProductRequest(
    @field:NotBlank
    val name: String,

    @field:NotBlank
    val description: String,

    @field:NotNull
    val type: ProductType,

    @field:NotNull
    @field:PositiveOrZero
    val basePrice: BigDecimal,

    val currency: String = "EUR",
    val variants: List<ProductVariantRequest> = emptyList(),
    val initialStock: Int = 0,
    val trackInventory: Boolean = true,
    val categoryId: Long? = null,
    val tags: Set<String> = emptySet(),
    val metadata: Map<String, String> = emptyMap()
)