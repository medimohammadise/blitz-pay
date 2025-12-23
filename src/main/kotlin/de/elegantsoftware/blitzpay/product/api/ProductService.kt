package de.elegantsoftware.blitzpay.product.api

import de.elegantsoftware.blitzpay.product.domain.Product
import de.elegantsoftware.blitzpay.product.domain.ProductId
import de.elegantsoftware.blitzpay.product.domain.ProductStatus
import de.elegantsoftware.blitzpay.product.domain.ProductType
import de.elegantsoftware.blitzpay.product.domain.ProductVariant
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.math.BigDecimal
import java.util.*

interface ProductService {
    fun createProduct(
        merchantId: Long,
        name: String,
        description: String,
        type: ProductType,
        basePrice: BigDecimal,
        currency: String,
        variants: List<ProductVariant>,
        initialStock: Int,
        trackInventory: Boolean,
        categoryId: Long?,
        tags: Set<String>,
        metadata: Map<String, String>
    ): Product

    fun updateProduct(
        merchantId: Long,
        productPublicId: UUID,
        name: String?,
        description: String?,
        type: ProductType?,
        basePrice: BigDecimal?,
        currency: String?,
        variants: List<ProductVariant>?,
        categoryId: Long?,
        tags: Set<String>?,
        metadata: Map<String, String>?
    ): Product

    fun getProduct(merchantId: Long, productPublicId: UUID): Product
    fun getProductById(productId: ProductId): Product
    fun getProductsByIds(productIds: List<UUID>): List<Product>
    fun getTotalPrice(productIds: List<UUID>): BigDecimal

    fun getProductsByPublicIds(merchantId: Long, productPublicIds: List<UUID>): List<Product>
    fun listProducts(merchantId: Long, pageable: Pageable): Page<Product>
    fun searchProducts(
        merchantId: Long,
        query: String,
        status: ProductStatus?,
        type: ProductType?,
        pageable: Pageable
    ): Page<Product>

    fun deleteProduct(merchantId: Long, productPublicId: UUID)
    fun changeProductStatus(merchantId: Long, productPublicId: UUID, status: ProductStatus)
    fun adjustStock(merchantId: Long, productPublicId: UUID, adjustment: Int): Product
    fun updatePrice(merchantId: Long, productPublicId: UUID, newPrice: BigDecimal, currency: String): Product
}

