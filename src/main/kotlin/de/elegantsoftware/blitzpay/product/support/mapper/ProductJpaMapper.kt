package de.elegantsoftware.blitzpay.product.support.mapper

import de.elegantsoftware.blitzpay.product.domain.Product
import de.elegantsoftware.blitzpay.product.domain.ProductId
import de.elegantsoftware.blitzpay.product.domain.ProductInventory
import de.elegantsoftware.blitzpay.product.domain.Price
import de.elegantsoftware.blitzpay.product.domain.ProductVariant
import de.elegantsoftware.blitzpay.product.outbound.persistence.ProductJpaEntity
import de.elegantsoftware.blitzpay.product.outbound.persistence.ProductInventoryEmbeddable
import de.elegantsoftware.blitzpay.product.outbound.persistence.ProductVariantJpaEntity
import org.springframework.stereotype.Component

@Component
class ProductJpaMapper {

    fun toEntity(product: Product): ProductJpaEntity {
        return ProductJpaEntity(
            id = if (product.id.value == 0L) null else product.id.value,
            publicId = product.publicId,
            merchantId = product.merchantId,
            name = product.name,
            description = product.description,
            type = product.type,
            status = product.status,
            basePriceAmount = product.basePrice.amount,
            basePriceCurrency = product.basePrice.currency,
            basePriceTaxInclusive = product.basePrice.taxInclusive,
            inventory = product.inventory.toEmbeddable(),
            categoryId = product.categoryId,
            createdAt = product.createdAt,
            updatedAt = product.updatedAt
        ).apply {
            // Initialize collections
            tags.addAll(product.tags)
            metadata.putAll(product.metadata)

            // Add variants with the relationship
            product.variants.forEach { variant ->
                variants.add(variant.toEntity(this))
            }
        }
    }

    fun updateEntity(existingEntity: ProductJpaEntity, product: Product): ProductJpaEntity {
        // Update basic fields
        existingEntity.name = product.name
        existingEntity.description = product.description
        existingEntity.type = product.type
        existingEntity.status = product.status
        existingEntity.basePriceAmount = product.basePrice.amount
        existingEntity.basePriceCurrency = product.basePrice.currency
        existingEntity.basePriceTaxInclusive = product.basePrice.taxInclusive
        existingEntity.inventory = product.inventory.toEmbeddable()
        existingEntity.categoryId = product.categoryId
        existingEntity.updatedAt = product.updatedAt

        // Update tags
        existingEntity.tags.clear()
        existingEntity.tags.addAll(product.tags)

        // Update metadata
        existingEntity.metadata.clear()
        existingEntity.metadata.putAll(product.metadata)

        // Update variants - this is complex due to relationships
        updateVariants(existingEntity, product.variants)

        return existingEntity
    }

    private fun updateVariants(entity: ProductJpaEntity, domainVariants: List<ProductVariant>) {
        // Create a map of existing variants by SKU for efficient lookup
        val existingVariantsBySku = entity.variants.associateBy { it.sku }

        // Process domain variants
        val updatedVariants = mutableListOf<ProductVariantJpaEntity>()

        domainVariants.forEach { domainVariant ->
            val existingVariant = existingVariantsBySku[domainVariant.sku]

            if (existingVariant != null) {
                // Update existing variant
                existingVariant.name = domainVariant.name
                existingVariant.priceAmount = domainVariant.price.amount
                existingVariant.priceCurrency = domainVariant.price.currency
                existingVariant.priceTaxInclusive = domainVariant.price.taxInclusive
                existingVariant.attributes.clear()
                existingVariant.attributes.putAll(domainVariant.attributes)
                updatedVariants.add(existingVariant)
            } else {
                // Create new variant
                val newVariant = domainVariant.toEntity(entity)
                updatedVariants.add(newVariant)
            }
        }

        // Clear and replace the variants collection
        entity.variants.clear()
        entity.variants.addAll(updatedVariants)
    }

    fun toDomain(entity: ProductJpaEntity): Product {
        return Product(
            id = ProductId(entity.id!!),
            publicId = entity.publicId,
            merchantId = entity.merchantId,
            name = entity.name,
            description = entity.description,
            type = entity.type,
            status = entity.status,
            basePrice = entity.toPrice(),
            variants = entity.variants.map { it.toDomain() },
            inventory = entity.inventory.toDomain(),
            categoryId = entity.categoryId,
            tags = entity.tags.toSet(),
            metadata = entity.metadata.toMap(),
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    // Extension functions for cleaner code
    private fun ProductInventory.toEmbeddable(): ProductInventoryEmbeddable {
        return ProductInventoryEmbeddable(
            quantity = this.quantity,
            lowStockThreshold = this.lowStockThreshold,
            isTracked = this.isTracked
        )
    }

    private fun ProductInventoryEmbeddable?.toDomain(): ProductInventory {
        return ProductInventory(
            quantity = this?.quantity ?: 0,
            lowStockThreshold = this?.lowStockThreshold ?: 10,
            isTracked = this?.isTracked ?: true
        )
    }

    private fun ProductVariant.toEntity(product: ProductJpaEntity): ProductVariantJpaEntity {
        return ProductVariantJpaEntity(
            variantId = this.id,
            product = product,
            sku = this.sku,
            name = this.name,
            priceAmount = this.price.amount,
            priceCurrency = this.price.currency,
            priceTaxInclusive = this.price.taxInclusive
        ).apply {
            this.attributes.putAll(this@toEntity.attributes)
        }
    }

    private fun ProductVariantJpaEntity.toDomain(): ProductVariant {
        return ProductVariant(
            id = this.variantId,
            sku = this.sku,
            name = this.name,
            price = Price(
                amount = this.priceAmount,
                currency = this.priceCurrency,
                taxInclusive = this.priceTaxInclusive
            ),
            attributes = this.attributes.toMap()
        )
    }

    private fun ProductJpaEntity.toPrice(): Price {
        return Price(
            amount = this.basePriceAmount,
            currency = this.basePriceCurrency,
            taxInclusive = this.basePriceTaxInclusive
        )
    }
}