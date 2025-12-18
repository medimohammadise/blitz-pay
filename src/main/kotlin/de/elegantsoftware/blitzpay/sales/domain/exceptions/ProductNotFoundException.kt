package de.elegantsoftware.blitzpay.sales.domain.exceptions

class ProductNotFoundException(productId: Long) :
    RuntimeException("Product with ID $productId not found")