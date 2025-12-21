package de.elegantsoftware.blitzpay.merchant.support.mapper

import de.elegantsoftware.blitzpay.merchant.domain.Merchant
import de.elegantsoftware.blitzpay.merchant.inbound.web.dto.CreateMerchantRequest
import de.elegantsoftware.blitzpay.merchant.inbound.web.dto.MerchantResponse
import de.elegantsoftware.blitzpay.merchant.inbound.web.dto.MerchantSettingsResponse
import de.elegantsoftware.blitzpay.merchant.inbound.web.dto.NotificationPreferencesResponse
import org.springframework.stereotype.Component

@Component
class MerchantMapper {

    fun toResponse(merchant: Merchant): MerchantResponse {
        return MerchantResponse(
            id = merchant.publicId,
            email = merchant.email,
            businessName = merchant.businessName,
            status = merchant.status,
            isActive = merchant.isActive(),
            isEmailVerified = merchant.isEmailVerified(),
            createdAt = merchant.createdAt,
            updatedAt = merchant.updatedAt,
            settings = MerchantSettingsResponse(
                defaultCurrency = merchant.settings.defaultCurrency,
                language = merchant.settings.language,
                notificationPreferences = NotificationPreferencesResponse(
                    emailNotifications = merchant.settings.notificationPreferences.emailNotifications,
                    smsNotifications = merchant.settings.notificationPreferences.smsNotifications
                )
            )
        )
    }

    fun toDomain(request: CreateMerchantRequest): Triple<String, String, de.elegantsoftware.blitzpay.merchant.domain.MerchantSettings> {
        return Triple(
            request.email,
            request.businessName,
            de.elegantsoftware.blitzpay.merchant.domain.MerchantSettings(
                defaultCurrency = request.defaultCurrency ?: "EUR",
                language = request.language ?: "en",
                notificationPreferences = de.elegantsoftware.blitzpay.merchant.domain.MerchantSettings.NotificationPreferences(
                    emailNotifications = request.emailNotifications ?: true,
                    smsNotifications = request.smsNotifications ?: false
                )
            )
        )
    }
}