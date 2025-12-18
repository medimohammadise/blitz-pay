package de.elegantsoftware.blitzpay.merchant.support.mapper

import de.elegantsoftware.blitzpay.merchant.domain.Merchant
import de.elegantsoftware.blitzpay.merchant.inbound.web.dto.MerchantResponse
import de.elegantsoftware.blitzpay.merchant.inbound.web.dto.MerchantSettingsResponse
import org.springframework.stereotype.Component

@Component
class MerchantMapper {

    fun toResponse(merchant: Merchant): MerchantResponse {
        return MerchantResponse(
            id = merchant.id,
            publicId = merchant.publicId,
            email = merchant.email,
            businessName = merchant.businessName,
            status = merchant.status.name,
            settings = MerchantSettingsResponse(
                defaultCurrency = merchant.settings.defaultCurrency,
                webhookUrl = merchant.settings.webhookUrl
            ),
            verifiedAt = merchant.verifiedAt,
            createdAt = merchant.createdAt,
            updatedAt = merchant.updatedAt
        )
    }
}