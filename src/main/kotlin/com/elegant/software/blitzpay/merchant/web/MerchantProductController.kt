package com.elegant.software.blitzpay.merchant.web

import com.elegant.software.blitzpay.merchant.api.CreateProductRequest
import com.elegant.software.blitzpay.merchant.api.ProductImageUploadUrlRequest
import com.elegant.software.blitzpay.merchant.api.ProductImageUploadUrlResponse
import com.elegant.software.blitzpay.merchant.api.ProductListResponse
import com.elegant.software.blitzpay.merchant.api.ProductResponse
import com.elegant.software.blitzpay.merchant.api.UpdateProductRequest
import com.elegant.software.blitzpay.merchant.application.MerchantProductService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.UUID

@Tag(name = "Merchant Products", description = "Product catalog management for merchants")
@RestController
@RequestMapping("/{version:v\\d+(?:\\.\\d+)*}/merchants/{merchantId}/products", version = "1")
class MerchantProductController(
    private val merchantProductService: MerchantProductService
) {

    @Operation(
        summary = "Create a product for the merchant",
        description = "Adds a new active product to the merchant's catalog. " +
            "imageStorageKeys is an ordered list of S3/MinIO storage keys obtained from the upload-url endpoint — " +
            "first entry is treated as the primary image. Returns 404 if the merchant does not exist."
    )
    @PostMapping
    fun create(
        @PathVariable merchantId: UUID,
        @RequestBody request: CreateProductRequest
    ): Mono<ResponseEntity<ProductResponse>> =
        Mono.fromCallable { merchantProductService.create(merchantId, request) }
            .subscribeOn(Schedulers.boundedElastic())
            .map { ResponseEntity.status(HttpStatus.CREATED).body(it) }

    @Operation(
        summary = "List all active products for the merchant",
        description = "Returns only products where active = true. Returns an empty list (not 404) when the merchant has no active products."
    )
    @GetMapping
    fun list(@PathVariable merchantId: UUID): Mono<ResponseEntity<ProductListResponse>> =
        Mono.fromCallable { merchantProductService.list(merchantId) }
            .subscribeOn(Schedulers.boundedElastic())
            .map { ResponseEntity.ok(it) }

    @Operation(
        summary = "Get a single active product",
        description = "Returns 404 if the product does not exist, belongs to a different merchant, or has been deactivated."
    )
    @GetMapping("/{productId}")
    fun get(
        @PathVariable merchantId: UUID,
        @PathVariable productId: UUID
    ): Mono<ResponseEntity<ProductResponse>> =
        Mono.fromCallable { merchantProductService.get(merchantId, productId) }
            .subscribeOn(Schedulers.boundedElastic())
            .map { ResponseEntity.ok(it) }

    @Operation(
        summary = "Update a product's name, price, and image",
        description = "Full replacement of mutable fields. " +
            "imageStorageKeys replaces the entire image list in the order provided — omit or pass [] to remove all images. " +
            "Response imageUrls are short-lived pre-signed download URLs resolved at request time."
    )
    @PutMapping("/{productId}")
    fun update(
        @PathVariable merchantId: UUID,
        @PathVariable productId: UUID,
        @RequestBody request: UpdateProductRequest
    ): Mono<ResponseEntity<ProductResponse>> =
        Mono.fromCallable { merchantProductService.update(merchantId, productId, request) }
            .subscribeOn(Schedulers.boundedElastic())
            .map { ResponseEntity.ok(it) }

    @Operation(
        summary = "Deactivate a product",
        description = "Soft-delete — sets active = false. The product is excluded from list/get responses. Returns 204 on success."
    )
    @DeleteMapping("/{productId}")
    fun deactivate(
        @PathVariable merchantId: UUID,
        @PathVariable productId: UUID
    ): Mono<ResponseEntity<Void>> =
        Mono.fromCallable { merchantProductService.deactivate(merchantId, productId) }
            .subscribeOn(Schedulers.boundedElastic())
            .map { ResponseEntity.noContent().build<Void>() }

    @Operation(
        summary = "Get a pre-signed upload URL for a product image",
        description = "Returns a short-lived S3/MinIO PUT URL and the storageKey to register on the product. " +
            "Flow: call this endpoint → upload binary directly to uploadUrl → include storageKey in create/update request. " +
            "Default contentType is image/jpeg. URL expires in 15 minutes."
    )
    @PostMapping("/{productId}/images/upload-url")
    fun imageUploadUrl(
        @PathVariable merchantId: UUID,
        @PathVariable productId: UUID,
        @RequestBody(required = false) request: ProductImageUploadUrlRequest?
    ): Mono<ResponseEntity<ProductImageUploadUrlResponse>> =
        Mono.fromCallable {
            merchantProductService.presignImageUpload(
                merchantId, productId, request?.contentType ?: "image/jpeg"
            )
        }
            .subscribeOn(Schedulers.boundedElastic())
            .map { ResponseEntity.ok(it) }
}
