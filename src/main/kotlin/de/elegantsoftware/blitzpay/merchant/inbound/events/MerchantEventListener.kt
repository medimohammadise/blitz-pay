package de.elegantsoftware.blitzpay.merchant.inbound.events

import de.elegantsoftware.blitzpay.merchant.domain.events.MerchantRegistered
import de.elegantsoftware.blitzpay.merchant.domain.events.MerchantRegistrationCompleted
import de.elegantsoftware.blitzpay.merchant.domain.events.MerchantVerificationEmailRequested
import de.elegantsoftware.blitzpay.merchant.support.config.MerchantProperties
import org.slf4j.LoggerFactory
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
class MerchantEventListener(
    private val merchantProperties: MerchantProperties
) {

    private val logger = LoggerFactory.getLogger(MerchantEventListener::class.java)

    @ApplicationModuleListener
    fun onMerchantRegistered(event: MerchantRegistered) {
        logger.info("Merchant registered event received: ${event.publicId}")

        // Send verification email (log for development)
        val verificationUrl = "${merchantProperties.verificationBaseUrl}/api/merchants/verification?token=${event.publicId}"
        logger.info("Sending verification email to: ${event.email}")
        logger.info("onMerchantRegistered -> Verification URL: $verificationUrl")

        // In production, you would:
        // 1. Generate a secure verification token (JWT with expiration)
        // 2. Store it in the database
        // 3. Send email with the token
        // 4. Verify the token when the endpoint is called

        // For now, we're using the publicId as the verification token
    }

    @ApplicationModuleListener
    fun onVerificationEmailRequested(event: MerchantVerificationEmailRequested) {
        logger.info("Resending verification email for merchant: ${event.publicId}")

        val verificationUrl = "${merchantProperties.verificationBaseUrl}/api/merchants/verification?token=${event.publicId}"
        logger.info("Resending verification email to: ${event.email}")
        logger.info("onVerificationEmailRequested -> Verification URL: $verificationUrl")

        // In production:
        // emailService.sendVerificationEmail(event.email, verificationUrl)
    }

    @ApplicationModuleListener
    fun onMerchantRegistrationCompleted(event: MerchantRegistrationCompleted) {
        logger.info("Merchant profile completed: ${event.publicId}")
        // Trigger side effects: welcome email, onboarding, etc.
    }
}