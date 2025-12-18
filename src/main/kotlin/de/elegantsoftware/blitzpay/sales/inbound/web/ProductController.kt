package de.elegantsoftware.blitzpay.sales.inbound.web

import de.elegantsoftware.blitzpay.sales.api.ProductServicePort
import de.elegantsoftware.blitzpay.sales.inbound.web.dto.*
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/products")
@Tag(
    name = "Products",
    description = "Products management APIs"
)
class ProductController(
    private val productService: ProductServicePort
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createProduct(@Valid @RequestBody request: CreateProductRequest): ProductResponse {
        return productService.createProduct(request)
    }

    @GetMapping("/{id}")
    fun getProduct(@PathVariable id: UUID): ProductResponse {
        return productService.getProduct(id)
    }

    @PutMapping("/{id}")
    fun updateProduct(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateProductRequest
    ): ProductResponse {
        return productService.updateProduct(id, request)
    }

    @PatchMapping("/{id}/stock")
    fun updateStock(
        @PathVariable id: UUID,
        @RequestParam quantity: Int
    ): ProductResponse {
        return productService.updateStock(id, quantity)
    }

    @PostMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deactivateProduct(@PathVariable id: UUID) {
        productService.deactivateProduct(id)
    }
}

