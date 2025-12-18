package de.elegantsoftware.blitzpay.merchant.support.exception

import de.elegantsoftware.blitzpay.common.api.exceptions.*

class MerchantNotFoundException(
    message: String = "Merchant not found"
) : NotFoundException(message, "MERCHANT_NOT_FOUND")

class MerchantAlreadyExistsException(
    message: String = "Merchant with this email already exists"
) : ConflictException(message, "MERCHANT_ALREADY_EXISTS")

class MerchantInvalidStatusException(
    message: String = "Merchant status is invalid for this operation"
) : BadRequestException(message, "INVALID_MERCHANT_STATUS")

class MerchantVerificationException(
    message: String = "Merchant verification failed"
) : BadRequestException(message, "VERIFICATION_FAILED")