package de.elegantsoftware.blitzpay.sales.inbound.web.dto

import java.math.BigDecimal
import java.util.UUID

data class ProductSearchCriteria(
    val nameContains: String? = null,
    val category: String? = null,
    val minPrice: BigDecimal? = null,
    val maxPrice: BigDecimal? = null,
    val inStock: Boolean? = null,
    val status: String? = null,
    val merchantId: UUID
)