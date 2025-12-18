package de.elegantsoftware.blitzpay.merchant.domain

import de.elegantsoftware.blitzpay.common.domain.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "merchants")
class Merchant(
    @Column(unique = true, nullable = false)
    val email: String,

    @Column(nullable = false)
    var businessName: String,

    @Embedded
    var settings: MerchantSettings = MerchantSettings(),

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    var status: MerchantStatus = MerchantStatus.PENDING_VERIFICATION,

    // New field: Track verification if needed
    @Column(name = "verified_at")
    var verifiedAt: LocalDateTime? = null,


) : BaseEntity() {
    companion object {
        fun create(
            email: String,
            businessName: String,
            settings: MerchantSettings = MerchantSettings()
        ): Merchant {
            // Validate business rules before creating
            require(email.isNotBlank()) { "Email must not be blank" }
            require(businessName.isNotBlank()) { "Business name must not be blank" }
            require(email.contains("@")) { "Invalid email format" }

            return Merchant(
                email = email.trim(),
                businessName = businessName.trim(),
                settings = settings,
            )
        }
    }

    fun verify() {
        require(status == MerchantStatus.PENDING_VERIFICATION) {
            "Merchant is already ${status.name.lowercase()}"
        }
        status = MerchantStatus.ACTIVE
        verifiedAt = LocalDateTime.now()
    }

    fun deactivate() {
        status = MerchantStatus.INACTIVE
    }

    fun updateSettings(newSettings: MerchantSettings) {
        this.settings = newSettings
    }
}