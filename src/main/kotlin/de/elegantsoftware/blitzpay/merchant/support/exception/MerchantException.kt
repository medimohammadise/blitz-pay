package de.elegantsoftware.blitzpay.merchant.support.exception

import java.util.UUID

enum class MerchantErrorCode {
    MERCHANT_NOT_FOUND,
    MERCHANT_ALREADY_EXISTS,
    MERCHANT_INACTIVE,
    INVALID_MERCHANT_STATUS,
    EMAIL_VERIFICATION_FAILED,
    VERIFICATION_TOKEN_INVALID,
    BUSINESS_NAME_INVALID
}

open class MerchantException(
    val errorCode: MerchantErrorCode,
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

class MerchantNotFoundException(
    merchantId: de.elegantsoftware.blitzpay.merchant.domain.MerchantId? = null,
    email: String? = null,
    publicId: UUID? = null
) : MerchantException(
    errorCode = MerchantErrorCode.MERCHANT_NOT_FOUND,
    message = when {
        merchantId != null -> "Merchant with ID $merchantId not found"
        email != null -> "Merchant with email $email not found"
        publicId != null -> "Merchant with public ID $publicId not found"
        else -> "Merchant not found"
    }
)

class MerchantAlreadyExistsException(
    email: String
) : MerchantException(
    errorCode = MerchantErrorCode.MERCHANT_ALREADY_EXISTS,
    message = "Merchant with email $email already exists"
)

class MerchantInactiveException(
    merchantId: de.elegantsoftware.blitzpay.merchant.domain.MerchantId
) : MerchantException(
    errorCode = MerchantErrorCode.MERCHANT_INACTIVE,
    message = "Merchant with ID $merchantId is inactive"
)

class InvalidMerchantStatusException(
    currentStatus: de.elegantsoftware.blitzpay.merchant.domain.MerchantStatus,
    expectedStatus: de.elegantsoftware.blitzpay.merchant.domain.MerchantStatus
) : MerchantException(
    errorCode = MerchantErrorCode.INVALID_MERCHANT_STATUS,
    message = "Merchant status is $currentStatus but expected $expectedStatus"
)

class VerificationTokenInvalidException(
    token: UUID
) : MerchantException(
    errorCode = MerchantErrorCode.VERIFICATION_TOKEN_INVALID,
    message = "Verification token $token is invalid or expired"
)

class BusinessNameInvalidException(
    message: String
) : MerchantException(
    errorCode = MerchantErrorCode.BUSINESS_NAME_INVALID,
    message = message
)