package de.elegantsoftware.blitzpay.product.inbound.web.dto

import java.util.UUID

data class ProductVariantResponse(
    val id: UUID,
    val sku: String,
    val name: String,
    val price: PriceResponse,
    val attributes: Map<String, String>
)