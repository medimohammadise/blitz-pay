package de.elegantsoftware.blitzpay.product

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class CreateProductRequest(
    val productName: String,
    val merchantId: Long
)

@RestController
@RequestMapping("/api/products")
class ProductController(private val productService: ProductService) {

    @PostMapping
    fun create(@RequestBody request: CreateProductRequest): ResponseEntity<Product> =
        ResponseEntity.ok(
            productService.create(
                productName = request.productName,
                merchantId = request.merchantId
            )
        )

    @GetMapping
    fun getAll(): ResponseEntity<List<Product>> =
        ResponseEntity.ok(productService.findAll())

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<Product> =
        ResponseEntity.ok(productService.findById(id))

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        productService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
