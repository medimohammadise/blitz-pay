package de.elegantsoftware.blitzpay.merchant.support.mapper

import de.elegantsoftware.blitzpay.merchant.domain.*
import de.elegantsoftware.blitzpay.merchant.outbound.persistence.MerchantJpaEntity
import de.elegantsoftware.blitzpay.merchant.outbound.persistence.MerchantStatusEntity
import org.springframework.stereotype.Component

@Component
class MerchantJpaMapper {

    fun toEntity(merchant: Merchant): MerchantJpaEntity {
        return MerchantJpaEntity(
            id = merchant.id.value,
            publicId = merchant.publicId,
            email = merchant.email,
            businessName = merchant.businessName,
            defaultCurrency = merchant.settings.defaultCurrency,
            language = merchant.settings.language,
            emailNotifications = merchant.settings.notificationPreferences.emailNotifications,
            smsNotifications = merchant.settings.notificationPreferences.smsNotifications,
            marketingEmails = merchant.settings.notificationPreferences.marketingEmails,
            defaultPaymentMethod = merchant.settings.paymentPreferences.defaultPaymentMethod,
            allowedCurrencies = merchant.settings.paymentPreferences.allowedCurrencies.joinToString(","),
            autoSettle = merchant.settings.paymentPreferences.autoSettle,
            status = MerchantStatusEntity.valueOf(merchant.status.name),
            emailVerifiedAt = merchant.emailVerifiedAt,
            createdAt = merchant.createdAt,
            updatedAt = merchant.updatedAt
        )
    }

    fun toDomain(entity: MerchantJpaEntity): Merchant {
        return Merchant.reconstruct(
            id = MerchantId(entity.id),
            publicId = entity.publicId,
            email = entity.email,
            businessName = entity.businessName,
            settings = MerchantSettings(
                defaultCurrency = entity.defaultCurrency,
                language = entity.language,
                notificationPreferences = MerchantSettings.NotificationPreferences(
                    emailNotifications = entity.emailNotifications,
                    smsNotifications = entity.smsNotifications,
                    marketingEmails = entity.marketingEmails
                ),
                paymentPreferences = MerchantSettings.PaymentPreferences(
                    defaultPaymentMethod = entity.defaultPaymentMethod,
                    allowedCurrencies = entity.allowedCurrencies.split(",").toSet(),
                    autoSettle = entity.autoSettle
                )
            ),
            status = MerchantStatus.valueOf(entity.status.name),
            emailVerifiedAt = entity.emailVerifiedAt,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )
    }
}