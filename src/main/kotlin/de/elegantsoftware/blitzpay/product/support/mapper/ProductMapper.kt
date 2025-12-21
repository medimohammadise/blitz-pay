package de.elegantsoftware.blitzpay.product.support.mapper

import de.elegantsoftware.blitzpay.product.domain.Product
import de.elegantsoftware.blitzpay.product.inbound.web.dto.*
import org.springframework.stereotype.Component

@Component
class ProductMapper {

    fun toResponse(product: Product): ProductResponse {
        return ProductResponse(
            publicId = product.publicId,
            merchantId = product.merchantId,
            name = product.name,
            description = product.description,
            type = product.type,
            status = product.status,
            basePrice = PriceResponse(
                amount = product.basePrice.amount,
                currency = product.basePrice.currency,
                taxInclusive = product.basePrice.taxInclusive
            ),
            variants = product.variants.map { variant ->
                ProductVariantResponse(
                    id = variant.id,
                    sku = variant.sku,
                    name = variant.name,
                    price = PriceResponse(
                        amount = variant.price.amount,
                        currency = variant.price.currency,
                        taxInclusive = variant.price.taxInclusive
                    ),
                    attributes = variant.attributes
                )
            },
            inventory = ProductInventoryResponse(
                quantity = product.inventory.quantity,
                lowStockThreshold = product.inventory.lowStockThreshold,
                isTracked = product.inventory.isTracked
            ),
            categoryId = product.categoryId,
            tags = product.tags,
            metadata = product.metadata,
            createdAt = product.createdAt,
            updatedAt = product.updatedAt
        )
    }
}