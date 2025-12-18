package de.elegantsoftware.blitzpay.merchant.api

import de.elegantsoftware.blitzpay.merchant.domain.Merchant

interface MerchantEventPublisher {
    fun publishMerchantCreated(merchant: Merchant)
    fun publishMerchantVerified(merchant: Merchant)
    fun publishMerchantDeactivated(merchant: Merchant)
}
