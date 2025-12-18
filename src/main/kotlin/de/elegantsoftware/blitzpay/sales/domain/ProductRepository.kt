package de.elegantsoftware.blitzpay.sales.domain

import org.springframework.data.repository.Repository
import java.util.*

interface ProductRepository : Repository<Product, Long> {
    fun save(product: Product): Product
    fun findById(id: Long): Optional<Product>  // Internal ID
    fun findByPublicId(publicId: UUID): Optional<Product>  // Public ID for API
    fun findBySku(sku: String): Optional<Product>
    fun findByMerchantIdAndSku(merchantId: Long, sku: String): Optional<Product>
    fun findByMerchantPublicIdAndSku(merchantPublicId: UUID, sku: String): Optional<Product>
    fun findAllByStatus(status: ProductStatus): List<Product>
    fun findAllByIdIn(ids: Collection<Long>): List<Product>
    fun existsBySku(sku: String): Boolean
    fun existsByMerchantIdAndSku(merchantId: Long, sku: String): Boolean
    fun findAllByCategoriesContaining(category: String): List<Product>
    fun findAllByMerchantId(merchantId: Long): List<Product>
    fun findAllByMerchantPublicId(merchantPublicId: UUID): List<Product>
    fun findAllByMerchantIdAndStatus(merchantId: Long, status: ProductStatus): List<Product>
}