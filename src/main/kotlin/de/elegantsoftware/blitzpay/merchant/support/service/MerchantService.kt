package de.elegantsoftware.blitzpay.merchant.support.service

import de.elegantsoftware.blitzpay.common.api.exceptions.ValidationException
import de.elegantsoftware.blitzpay.merchant.api.MerchantEventPublisher
import de.elegantsoftware.blitzpay.merchant.api.MerchantServicePort
import de.elegantsoftware.blitzpay.merchant.domain.*
import de.elegantsoftware.blitzpay.merchant.inbound.web.dto.*
import de.elegantsoftware.blitzpay.merchant.support.exception.*
import de.elegantsoftware.blitzpay.merchant.support.mapper.MerchantMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class MerchantService(
    private val merchantRepository: MerchantRepository,
    private val merchantMapper: MerchantMapper,
    private val eventPublisher: MerchantEventPublisher
) : MerchantServicePort {

    override fun createMerchant(request: CreateMerchantRequest): MerchantResponse {
        // Check if merchant already exists
        if (merchantRepository.existsByEmail(request.email)) {
            throw MerchantAlreadyExistsException(
                "Merchant with email ${request.email} already exists"
            )
        }

        val merchant = Merchant.create(
            email = request.email,
            businessName = request.businessName,
            settings = MerchantSettings(
                defaultCurrency = request.defaultCurrency,
            )
        )

        val savedMerchant = merchantRepository.save(merchant)
        eventPublisher.publishMerchantCreated(savedMerchant)

        return merchantMapper.toResponse(savedMerchant)
    }

    override fun getMerchant(id: UUID): MerchantResponse {
        val merchant = merchantRepository.findByPublicId(id)
            .orElseThrow { MerchantNotFoundException("Merchant with id $id not found") }

        return merchantMapper.toResponse(merchant)
    }

    override fun verifyMerchant(id: UUID): MerchantResponse {
        val merchant = merchantRepository.findByPublicId(id)
            .orElseThrow { MerchantNotFoundException("Merchant with id $id not found") }

        try {
            merchant.verify()
        } catch (ex: IllegalStateException) {
            throw MerchantInvalidStatusException(
                "Cannot verify merchant: ${ex.message}"
            )
        }

        val verifiedMerchant = merchantRepository.save(merchant)
        eventPublisher.publishMerchantVerified(verifiedMerchant)

        return merchantMapper.toResponse(verifiedMerchant)
    }

    override fun updateMerchantSettings(id: UUID, settings: UpdateSettingsRequest) {
        val merchant = merchantRepository.findByPublicId(id)
            .orElseThrow { MerchantNotFoundException("Merchant with id $id not found") }

        // Validate currency if provided
        settings.defaultCurrency?.let { currency ->
            if (!isValidCurrency(currency)) {
                throw ValidationException("Invalid currency code: $currency")
            }
        }

        // Validate fee percentage if provided
        settings.transactionFeePercentage?.let { fee ->
            if (fee !in 0.0..100.0) {
                throw ValidationException("Transaction fee percentage must be between 0 and 100")
            }
        }

        val newSettings = merchant.settings.copy(
            defaultCurrency = settings.defaultCurrency ?: merchant.settings.defaultCurrency,
            webhookUrl = settings.webhookUrl ?: merchant.settings.webhookUrl
        )

        merchant.updateSettings(newSettings)
        merchantRepository.save(merchant)
    }

    override fun deactivateMerchant(id: UUID) {
        val merchant = merchantRepository.findByPublicId(id)
            .orElseThrow { MerchantNotFoundException("Merchant with id $id not found") }

        // Check if merchant can be deactivated
        if (merchant.status == MerchantStatus.INACTIVE) {
            throw MerchantInvalidStatusException("Merchant is already inactive")
        }

        merchant.deactivate()
        val deactivatedMerchant = merchantRepository.save(merchant)
        eventPublisher.publishMerchantDeactivated(deactivatedMerchant)
    }

    // Helper method to find merchant with proper exception
    private fun findMerchantOrThrow(id: UUID): Merchant {
        return merchantRepository.findByPublicId(id)
            .orElseThrow { MerchantNotFoundException("Merchant with id $id not found") }
    }

    // Helper method for currency validation
    private fun isValidCurrency(currency: String): Boolean {
        val validCurrencies = setOf("EUR", "USD", "GBP", "JPY", "CAD", "AUD")
        return currency.length == 3 && currency.uppercase() in validCurrencies
    }
}