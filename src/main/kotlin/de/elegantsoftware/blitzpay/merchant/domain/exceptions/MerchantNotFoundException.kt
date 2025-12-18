package de.elegantsoftware.blitzpay.merchant.domain.exceptions

import java.util.UUID

class MerchantNotFoundException(merchantId: UUID) :
    RuntimeException("Merchant with ID $merchantId not found")