package de.elegantsoftware.blitzpay.merchant.support

import de.elegantsoftware.blitzpay.merchant.api.MerchantService
import de.elegantsoftware.blitzpay.merchant.domain.*
import de.elegantsoftware.blitzpay.merchant.outbound.persistence.MerchantRepository
import de.elegantsoftware.blitzpay.merchant.outbound.persistence.MerchantStatusEntity
import de.elegantsoftware.blitzpay.merchant.support.exception.*
import de.elegantsoftware.blitzpay.merchant.support.mapper.MerchantJpaMapper
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class MerchantServiceImpl(
    private val merchantRepository: MerchantRepository,
    private val merchantMapper: MerchantJpaMapper,
    @param:Lazy private val self: MerchantService
) : MerchantService {

    private val logger = LoggerFactory.getLogger(MerchantServiceImpl::class.java)

    override fun registerMerchant(
        email: String,
        businessName: String?,
        settings: MerchantSettings
    ): Merchant {
        logger.info("Registering new merchant: $email")

        // Check if merchant already exists
        if (merchantRepository.existsByEmail(email)) {
            throw MerchantAlreadyExistsException(email)
        }

        // Create domain entity
        val merchant = Merchant.create(email, businessName, settings)

        // Convert to JPA entity and save
        val merchantEntity = merchantMapper.toEntity(merchant)
        val savedEntity = merchantRepository.save(merchantEntity)

        // Convert back to domain
        val savedMerchant = merchantMapper.toDomain(savedEntity)

        logger.info("Merchant registered successfully: ${savedMerchant.publicId}")
        return savedMerchant
    }

    override fun verifyMerchantEmail(verificationToken: UUID): Merchant {
        logger.info("Verifying merchant email with token: $verificationToken")

        val merchantEntity = merchantRepository.findByPublicId(verificationToken)
            ?: throw VerificationTokenInvalidException(verificationToken)

        // Convert to domain entity
        val merchant = merchantMapper.toDomain(merchantEntity)

        try {
            merchant.verifyEmail()

            // Update JPA entity status
            merchantEntity.status = MerchantStatusEntity.valueOf(merchant.status.name)
            merchantEntity.emailVerifiedAt = merchant.emailVerifiedAt
            merchantEntity.updatedAt = merchant.updatedAt

            val savedEntity = merchantRepository.save(merchantEntity)
            val verifiedMerchant = merchantMapper.toDomain(savedEntity)

            logger.info("Merchant email verified: ${merchant.publicId}")
            return verifiedMerchant
        } catch (e: IllegalArgumentException) {
            when (e.message) {
                "Merchant is already active" ->
                    throw InvalidMerchantStatusException(
                        merchant.status,
                        MerchantStatus.PENDING_VERIFICATION
                    )
                else -> throw e
            }
        }
    }

    override fun resendVerificationEmail(merchantId: MerchantId) {
        logger.info("Resending verification email for merchant: $merchantId")

        val merchant = self.getMerchant(merchantId)

        try {
            merchant.resendVerificationEmail()

            // The event will be published via domain events
            // We just need to save the merchant to trigger event publication
            val merchantEntity = merchantMapper.toEntity(merchant)
            merchantRepository.save(merchantEntity)
        } catch (e: IllegalArgumentException) {
            when (e.message?.contains("Cannot resend verification email")) {
                true -> throw InvalidMerchantStatusException(
                    merchant.status,
                    MerchantStatus.PENDING_VERIFICATION
                )
                else -> throw e
            }
        }
    }

    override fun getMerchant(merchantId: MerchantId): Merchant {
        val merchantEntity = merchantRepository.findById(merchantId.value)
            .orElseThrow { MerchantNotFoundException(merchantId = merchantId) }

        return merchantMapper.toDomain(merchantEntity)
    }

    override fun getMerchantByPublicId(publicId: UUID): Merchant {
        val merchantEntity = merchantRepository.findByPublicId(publicId)
            ?: throw MerchantNotFoundException(publicId = publicId)

        return merchantMapper.toDomain(merchantEntity)
    }

    override fun validateMerchant(publicId: UUID) {
        merchantRepository.findByPublicId(publicId)
            ?: throw MerchantNotFoundException(publicId = publicId)
    }

    override fun updateMerchantSettings(merchantId: MerchantId, settings: MerchantSettings): Merchant {
        val merchant = self.getMerchant(merchantId)

        if (!merchant.isActive()) {
            throw MerchantInactiveException(merchantId)
        }

        merchant.updateSettings(settings)

        // Update JPA entity
        val merchantEntity = merchantMapper.toEntity(merchant)
        val savedEntity = merchantRepository.save(merchantEntity)

        return merchantMapper.toDomain(savedEntity)
    }

    override fun updateMerchantBusinessName(merchantId: MerchantId, newName: String): Merchant {
        val merchant = self.getMerchant(merchantId)

        if (!merchant.isActive()) {
            throw MerchantInactiveException(merchantId)
        }

        try {
            merchant.updateBusinessName(newName)

            // Update JPA entity
            val merchantEntity = merchantMapper.toEntity(merchant)
            val savedEntity = merchantRepository.save(merchantEntity)

            return merchantMapper.toDomain(savedEntity)
        } catch (e: IllegalArgumentException) {
            throw BusinessNameInvalidException(e.message ?: "Invalid business name")
        }
    }

    override fun deactivateMerchant(merchantId: MerchantId): Merchant {
        val merchant = self.getMerchant(merchantId)
        merchant.deactivate()

        // Update JPA entity
        val merchantEntity = merchantMapper.toEntity(merchant)
        val savedEntity = merchantRepository.save(merchantEntity)

        return merchantMapper.toDomain(savedEntity)
    }

    override fun findActiveMerchants(): List<Merchant> {
        return merchantRepository.findByStatus("ACTIVE")
            .map { merchantMapper.toDomain(it) }
    }
}