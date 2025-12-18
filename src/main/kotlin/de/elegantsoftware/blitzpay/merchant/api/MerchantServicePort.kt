package de.elegantsoftware.blitzpay.merchant.api

import de.elegantsoftware.blitzpay.merchant.inbound.web.dto.*
import java.util.UUID

interface MerchantServicePort {
    fun createMerchant(request: CreateMerchantRequest): MerchantResponse
    fun getMerchant(id: UUID): MerchantResponse
    fun verifyMerchant(id: UUID): MerchantResponse
    fun updateMerchantSettings(id: UUID, settings: UpdateSettingsRequest)
    fun deactivateMerchant(id: UUID)
}