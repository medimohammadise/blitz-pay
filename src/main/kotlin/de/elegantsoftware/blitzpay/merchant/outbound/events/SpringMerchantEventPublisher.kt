package de.elegantsoftware.blitzpay.merchant.outbound.events

import de.elegantsoftware.blitzpay.merchant.api.MerchantEventPublisher
import de.elegantsoftware.blitzpay.merchant.domain.Merchant
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class SpringMerchantEventPublisher(
    private val eventPublisher: ApplicationEventPublisher
) : MerchantEventPublisher {

    override fun publishMerchantCreated(merchant: Merchant) {
        eventPublisher.publishEvent(MerchantCreatedEvent(merchant))
    }

    override fun publishMerchantVerified(merchant: Merchant) {
        eventPublisher.publishEvent(MerchantVerifiedEvent(merchant))
    }

    override fun publishMerchantDeactivated(merchant: Merchant) {
        eventPublisher.publishEvent(MerchantDeactivatedEvent(merchant))
    }
}

// Domain Events
sealed class MerchantEvent(val merchant: Merchant)
data class MerchantCreatedEvent(val merchantEntity: Merchant) : MerchantEvent(merchantEntity)
data class MerchantVerifiedEvent(val merchantEntity: Merchant) : MerchantEvent(merchantEntity)
data class MerchantDeactivatedEvent(val merchantEntity: Merchant) : MerchantEvent(merchantEntity)