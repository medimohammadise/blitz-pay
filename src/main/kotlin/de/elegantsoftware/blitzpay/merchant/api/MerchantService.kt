package de.elegantsoftware.blitzpay.merchant.api

import de.elegantsoftware.blitzpay.merchant.domain.*
import java.util.*

interface MerchantService {

    fun registerMerchant(
        email: String,
        businessName: String,
        settings: MerchantSettings = MerchantSettings()
    ): Merchant

    fun verifyMerchantEmail(verificationToken: UUID): Merchant

    fun resendVerificationEmail(merchantId: MerchantId)

    fun getMerchant(merchantId: MerchantId): Merchant

    fun getMerchantByPublicId(publicId: UUID): Merchant
    fun validateMerchant(publicId: UUID)

    fun updateMerchantSettings(merchantId: MerchantId, settings: MerchantSettings): Merchant

    fun updateMerchantBusinessName(merchantId: MerchantId, newName: String): Merchant

    fun deactivateMerchant(merchantId: MerchantId): Merchant

    fun findActiveMerchants(): List<Merchant>
}