package de.elegantsoftware.blitzpay.product.inbound.web

import de.elegantsoftware.blitzpay.product.api.ProductService
import de.elegantsoftware.blitzpay.product.domain.ProductStatus
import de.elegantsoftware.blitzpay.product.domain.ProductType
import de.elegantsoftware.blitzpay.product.domain.ProductVariant
import de.elegantsoftware.blitzpay.product.inbound.web.dto.CreateProductRequest
import de.elegantsoftware.blitzpay.product.inbound.web.dto.ProductResponse
import de.elegantsoftware.blitzpay.product.inbound.web.dto.UpdateProductRequest
import de.elegantsoftware.blitzpay.product.support.mapper.ProductMapper
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/merchants/{merchantId}/products")
@Tag(name = "Product Management", description = "APIs for managing products, including creation, updates, inventory, and catalog management")
class ProductController(
    private val productService: ProductService,
    private val productMapper: ProductMapper
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create product", description = "Creates a new product for a merchant with variants, pricing, and inventory management")
    fun createProduct(
        @PathVariable merchantId: Long,
        @Valid @RequestBody request: CreateProductRequest
    ): ProductResponse {
        val product = productService.createProduct(
            merchantId = merchantId,
            name = request.name,
            description = request.description,
            type = request.type,
            basePrice = request.basePrice,
            currency = request.currency,
            variants = request.variants.map {
                ProductVariant(
                    sku = it.sku,
                    name = it.name,
                    price = it.price,
                    attributes = it.attributes
                )
            },
            initialStock = request.initialStock,
            trackInventory = request.trackInventory,
            categoryId = request.categoryId,
            tags = request.tags,
            metadata = request.metadata
        )
        return productMapper.toResponse(product)
    }

    @PutMapping("/{productPublicId}")
    fun updateProduct(
        @PathVariable merchantId: Long,
        @PathVariable("productPublicId") productPublicId: UUID,
        @Valid @RequestBody request: UpdateProductRequest
    ): ProductResponse {
        val product = productService.updateProduct(
            merchantId = merchantId,
            productPublicId = productPublicId,
            name = request.name,
            description = request.description,
            type = request.type,
            basePrice = request.basePrice,
            currency = request.currency,
            variants = request.variants?.map {
                ProductVariant(
                    sku = it.sku,
                    name = it.name,
                    price = it.price,
                    attributes = it.attributes
                )
            },
            categoryId = request.categoryId,
            tags = request.tags,
            metadata = request.metadata
        )
        return productMapper.toResponse(product)
    }

    @GetMapping("/{productPublicId}")
    fun getProduct(
        @PathVariable merchantId: Long,
        @PathVariable productPublicId: UUID
    ): ProductResponse {
        val product = productService.getProduct(merchantId, productPublicId)
        return productMapper.toResponse(product)
    }

    @GetMapping
    fun listProducts(
        @PathVariable merchantId: Long,
        pageable: Pageable
    ): Page<ProductResponse> {
        return productService.listProducts(merchantId, pageable)
            .map { productMapper.toResponse(it) }
    }

    @GetMapping("/search")
    fun searchProducts(
        @PathVariable merchantId: Long,
        @RequestParam(required = false) query: String?,
        @RequestParam(required = false) status: ProductStatus?,
        @RequestParam(required = false) type: ProductType?,
        pageable: Pageable
    ): Page<ProductResponse> {
        return productService.searchProducts(
            merchantId = merchantId,
            query = query ?: "",
            status = status,
            type = type,
            pageable = pageable
        ).map { productMapper.toResponse(it) }
    }

    @DeleteMapping("/{productPublicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteProduct(
        @PathVariable merchantId: Long,
        @PathVariable productPublicId: UUID
    ) {
        productService.deleteProduct(merchantId, productPublicId)
    }

    @PatchMapping("/{productPublicId}/status")
    fun changeProductStatus(
        @PathVariable merchantId: Long,
        @PathVariable productPublicId: UUID,
        @RequestParam status: ProductStatus
    ) {
        productService.changeProductStatus(merchantId, productPublicId, status)
    }

    @PatchMapping("/{productPublicId}/stock")
    fun adjustStock(
        @PathVariable merchantId: Long,
        @PathVariable productPublicId: UUID,
        @RequestParam adjustment: Int
    ): ProductResponse {
        val product = productService.adjustStock(merchantId, productPublicId, adjustment)
        return productMapper.toResponse(product)
    }

    @PatchMapping("/{productPublicId}/price")
    fun updatePrice(
        @PathVariable merchantId: Long,
        @PathVariable productPublicId: UUID,
        @RequestParam newPrice: String,
        @RequestParam currency: String = "EUR"
    ): ProductResponse {
        val price = newPrice.toBigDecimal()
        val product = productService.updatePrice(merchantId, productPublicId, price, currency)
        return productMapper.toResponse(product)
    }
}