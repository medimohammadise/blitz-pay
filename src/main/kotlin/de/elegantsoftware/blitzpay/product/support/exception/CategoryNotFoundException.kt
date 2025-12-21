package de.elegantsoftware.blitzpay.product.support.exception

class CategoryNotFoundException(
    categoryId: Long? = null,
    name: String? = null
) : ProductException(
    errorCode = ProductErrorCode.CATEGORY_NOT_FOUND,
    message = when {
        categoryId != null -> "Category with ID $categoryId not found"
        name != null -> "Category with name '$name' not found"
        else -> "Category not found"
    }
)