package de.elegantsoftware.blitzpay.sales.support.exception

import de.elegantsoftware.blitzpay.common.api.exceptions.*

class ProductNotFoundException(
    message: String = "Product not found"
) : NotFoundException(message, "PRODUCT_NOT_FOUND")

class ProductAlreadyExistsException(
    message: String = "Product with this SKU already exists"
) : ConflictException(message, "PRODUCT_ALREADY_EXISTS")

class InsufficientStockException(
    message: String? = "Insufficient stock"
) : BadRequestException(message, "INSUFFICIENT_STOCK")

class InvoiceNotFoundException(
    message: String = "Invoice not found"
) : NotFoundException(message, "INVOICE_NOT_FOUND")

class InvoiceInvalidStatusException(
    message: String? = "Invoice status is invalid for this operation"
) : BadRequestException(message, "INVALID_INVOICE_STATUS")

class InvoiceAlreadyExistsException(
    message: String = "Invoice with this number already exists"
) : ConflictException(message, "INVOICE_ALREADY_EXISTS")