package de.elegantsoftware.blitzpay.sales.api

import de.elegantsoftware.blitzpay.sales.inbound.web.dto.*
import java.util.UUID

interface ProductServicePort {
    fun createProduct(request: CreateProductRequest): ProductResponse
    fun getProduct(id: UUID): ProductResponse
    fun updateProduct(id: UUID, request: UpdateProductRequest): ProductResponse
    fun updateStock(id: UUID, quantityChange: Int): ProductResponse
    fun deactivateProduct(id: UUID)
}