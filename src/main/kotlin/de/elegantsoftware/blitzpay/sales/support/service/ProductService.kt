package de.elegantsoftware.blitzpay.sales.support.service

import de.elegantsoftware.blitzpay.merchant.domain.MerchantRepository
import de.elegantsoftware.blitzpay.sales.api.ProductServicePort
import de.elegantsoftware.blitzpay.sales.api.SalesEventPublisher
import de.elegantsoftware.blitzpay.sales.domain.*
import de.elegantsoftware.blitzpay.sales.inbound.web.dto.*
import de.elegantsoftware.blitzpay.sales.support.exception.*
import de.elegantsoftware.blitzpay.sales.support.mapper.ProductMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class ProductService(
    private val productRepository: ProductRepository,
    private val merchantRepository: MerchantRepository,
    private val productMapper: ProductMapper,
    private val eventPublisher: SalesEventPublisher
) : ProductServicePort {

    override fun createProduct(request: CreateProductRequest): ProductResponse {
        // Find merchant by public ID
        val merchant = merchantRepository.findByPublicId(request.merchantId)
            .orElseThrow {
                de.elegantsoftware.blitzpay.merchant.support.exception.MerchantNotFoundException(
                    "Merchant with id ${request.merchantId} not found"
                )
            }

        // Check if product with same SKU exists for this merchant
        if (productRepository.existsByMerchantIdAndSku(merchant.id!!, request.sku)) {
            throw ProductAlreadyExistsException(
                "Product with SKU ${request.sku} already exists for merchant ${merchant.businessName}"
            )
        }

        val product = Product.create(
            merchant = merchant,
            sku = request.sku,
            name = request.name,
            price = Money(request.price, request.currency),
            initialStock = request.initialStock
        ).apply {
            description = request.description
            categories.addAll(request.categories)
            taxInfo = request.taxRate?.let { TaxInfo(it) }
        }

        val savedProduct = productRepository.save(product)
        eventPublisher.publishProductCreated(savedProduct)

        return productMapper.toResponse(savedProduct)
    }

    // Accept publicId for API
    override fun getProduct(id: UUID): ProductResponse {
        val product = productRepository.findByPublicId(id)
            .orElseThrow { ProductNotFoundException("Product with id $id not found") }

        return productMapper.toResponse(product)
    }

    // Internal method using public IDs
    fun getProductForMerchant(productPublicId: UUID, merchantPublicId: UUID): ProductResponse {
        val product = productRepository.findByPublicId(productPublicId)
            .orElseThrow { ProductNotFoundException("Product with id $productPublicId not found") }

        if (product.merchant.publicId != merchantPublicId) {
            throw ProductNotFoundException("Product not found for merchant")
        }

        return productMapper.toResponse(product)
    }

    override fun updateProduct(id: UUID, request: UpdateProductRequest): ProductResponse {
        val product = productRepository.findByPublicId(id)
            .orElseThrow { ProductNotFoundException("Product with id $id not found") }

        request.name?.let { product.name = it }
        request.description?.let { product.description = it }
        request.price?.let {
            product.updatePrice(Money(it, product.price.currency))
        }

        val updatedProduct = productRepository.save(product)
        return productMapper.toResponse(updatedProduct)
    }

    override fun updateStock(id: UUID, quantityChange: Int): ProductResponse {
        val product = productRepository.findByPublicId(id)
            .orElseThrow { ProductNotFoundException("Product with id $id not found") }

        try {
            product.updateStock(quantityChange)
        } catch (ex: IllegalArgumentException) {
            throw InsufficientStockException(ex.message)
        }

        val updatedProduct = productRepository.save(product)
        eventPublisher.publishProductStockUpdated(updatedProduct)

        return productMapper.toResponse(updatedProduct)
    }

    override fun deactivateProduct(id: UUID) {
        val product = productRepository.findByPublicId(id)
            .orElseThrow { ProductNotFoundException("Product with id $id not found") }

        product.deactivate()
        productRepository.save(product)
    }


    fun getMerchantProducts(merchantPublicId: UUID): List<ProductResponse> {
        val products = productRepository.findAllByMerchantPublicId(merchantPublicId)
        return products.map(productMapper::toResponse)
    }

    fun getMerchantActiveProducts(merchantPublicId: UUID): List<ProductResponse> {
        // Find merchant internal ID first
        val merchant = merchantRepository.findByPublicId(merchantPublicId)
            .orElseThrow {
                de.elegantsoftware.blitzpay.merchant.support.exception.MerchantNotFoundException(
                    "Merchant with id $merchantPublicId not found"
                )
            }

        val products = productRepository.findAllByMerchantIdAndStatus(merchant.id, ProductStatus.ACTIVE)
        return products.map(productMapper::toResponse)
    }
}