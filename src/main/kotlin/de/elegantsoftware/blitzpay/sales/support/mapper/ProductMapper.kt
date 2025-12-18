package de.elegantsoftware.blitzpay.sales.support.mapper

import de.elegantsoftware.blitzpay.sales.domain.Product
import de.elegantsoftware.blitzpay.sales.inbound.web.dto.ProductResponse
import org.springframework.stereotype.Component

@Component
class ProductMapper {

    fun toResponse(product: Product): ProductResponse {
        return ProductResponse(
            id = product.publicId,
            merchantId = product.merchant.publicId,
            sku = product.sku,
            name = product.name,
            description = product.description,
            price = product.price.amount,
            currency = product.price.currency,
            stockQuantity = product.stockQuantity,
            status = product.status.name,
            categories = product.categories,
            taxRate = product.taxInfo?.rate,
            isAvailable = product.isAvailable(),
            createdAt = product.createdAt,
            updatedAt = product.updatedAt,
            merchantName = product.merchant.businessName,
        )
    }
}