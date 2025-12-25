package de.elegantsoftware.blitzpay.merchant.domain

import de.elegantsoftware.blitzpay.merchant.domain.events.MerchantRegistered
import de.elegantsoftware.blitzpay.merchant.domain.events.MerchantVerificationEmailRequested
import de.elegantsoftware.blitzpay.merchant.domain.events.MerchantVerified
import org.springframework.data.domain.AbstractAggregateRoot
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Instant

class Merchant(
    val id: MerchantId,
    val publicId: UUID,
    val email: String,
    var businessName: String? = null,
    var settings: MerchantSettings,
    var status: MerchantStatus,
    var emailVerifiedAt: Instant? = null,
    var phoneNumber: String? = null,
    var businessAddress: String? = null,
    var city: String? = null,
    var postalCode: String? = null,
    var country: String? = null,
    var taxId: String? = null,
    var businessType: String? = null,
    var verifiedAt: Instant? = null,
    val createdAt: Instant,
    var updatedAt: Instant
) : AbstractAggregateRoot<Merchant>() {

    companion object {
        fun create(
            email: String,
            businessName: String? = null,
            settings: MerchantSettings = MerchantSettings()
        ): Merchant {
            require(email.isNotBlank()) { "Email must not be blank" }
            require(email.contains("@")) { "Invalid email format" }
            businessName?.let { require(it.isNotBlank()) { "Business name must not be blank" } }

            val merchant = Merchant(
                id = MerchantId(0),
                publicId = UUID.randomUUID(),
                email = email.trim(),
                businessName = businessName?.trim(),
                settings = settings,
                status = MerchantStatus.PENDING_VERIFICATION,
                emailVerifiedAt = null,
                createdAt = Clock.System.now(),
                updatedAt = Clock.System.now(),
            )

            merchant.registerEvent(
                MerchantRegistered(
                    merchantId = merchant.id,
                    publicId = merchant.publicId,
                    email = merchant.email,
                    businessName = merchant.businessName
                )
            )

            return merchant
        }

        fun reconstruct(
            id: MerchantId,
            publicId: UUID,
            email: String,
            businessName: String?,
            settings: MerchantSettings,
            status: MerchantStatus,
            emailVerifiedAt: Instant?,
            createdAt: Instant,
            updatedAt: Instant,
        ): Merchant {
            return Merchant(
                id = id,
                publicId = publicId,
                email = email,
                businessName = businessName,
                settings = settings,
                status = status,
                emailVerifiedAt = emailVerifiedAt,
                createdAt = createdAt,
                updatedAt = updatedAt,
            )
        }
    }

    fun verify() {
        require(status == MerchantStatus.PENDING_VERIFICATION) {
            "Merchant is already ${status.name.lowercase()}"
        }
        require(emailVerifiedAt != null) {
            "Email must be verified before merchant verification"
        }

        status = MerchantStatus.ACTIVE
        verifiedAt = Clock.System.now()
        updatedAt = Clock.System.now()

        registerEvent(
            MerchantVerified(
                merchantId = id,
                publicId = publicId
            )
        )
    }

    // This is not a companion object method, it's a regular class method
    fun reconstructWithId(id: MerchantId): Merchant {
        return Merchant(
            id = id,
            publicId = this.publicId,
            email = this.email,
            businessName = this.businessName,
            settings = this.settings,
            status = this.status,
            emailVerifiedAt = this.emailVerifiedAt,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )
    }

    fun verifyEmail() {
        require(status == MerchantStatus.PENDING_VERIFICATION) {
            "Merchant is already ${status.name.lowercase()}"
        }
        status = MerchantStatus.ACTIVE
        emailVerifiedAt = Clock.System.now()
        updatedAt = Clock.System.now()

        registerEvent(
            MerchantVerified(
                merchantId = id,
                publicId = publicId
            )
        )
    }

    fun completeRegistration(details: BusinessDetailsRequest) {
        require(emailVerifiedAt != null) {
            "Email must be verified before completing registration"
        }

        phoneNumber = details.phoneNumber
        businessAddress = details.businessAddress
        city = details.city
        postalCode = details.postalCode
        country = details.country
        taxId = details.taxId
        businessType = details.businessType

        updatedAt = Clock.System.now()
    }


    fun resendVerificationEmail() {
        require(status == MerchantStatus.PENDING_VERIFICATION) {
            "Cannot resend verification email. Merchant is ${status.name.lowercase()}"
        }

        registerEvent(
            MerchantVerificationEmailRequested(
                merchantId = id,
                publicId = publicId,
                email = email
            )
        )
    }

    fun deactivate() {
        status = MerchantStatus.INACTIVE
        updatedAt = Clock.System.now()
    }

    fun updateBusinessName(newName: String) {
        require(newName.isNotBlank()) { "Business name must not be blank" }
        businessName = newName.trim()
        updatedAt = Clock.System.now()
    }

    fun updateSettings(newSettings: MerchantSettings) {
        this.settings = newSettings
        updatedAt = Clock.System.now()
    }

    fun isActive(): Boolean = status == MerchantStatus.ACTIVE
    fun isEmailVerified(): Boolean = emailVerifiedAt != null
}