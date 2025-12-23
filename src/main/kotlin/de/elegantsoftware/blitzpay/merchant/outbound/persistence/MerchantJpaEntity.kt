package de.elegantsoftware.blitzpay.merchant.outbound.persistence

import jakarta.persistence.*
import java.util.*
import kotlin.time.Clock
import kotlin.time.Instant

@Entity
@Table(name = "merchants")
class MerchantJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "public_id", unique = true, nullable = false, updatable = false)
    val publicId: UUID = UUID.randomUUID(),

    @Column(unique = true, nullable = false)
    val email: String,

    @Column(nullable = true)
    var businessName: String?,

    @Column(name = "default_currency", length = 3)
    val defaultCurrency: String = "EUR",

    @Column(name = "language", length = 10)
    val language: String = "en",

    @Column(name = "email_notifications")
    val emailNotifications: Boolean = true,

    @Column(name = "sms_notifications")
    val smsNotifications: Boolean = false,

    @Column(name = "marketing_emails")
    val marketingEmails: Boolean = false,

    @Column(name = "default_payment_method", length = 50)
    val defaultPaymentMethod: String = "card",

    @Column(name = "allowed_currencies", length = 100)
    val allowedCurrencies: String = "EUR,USD,GBP",

    @Column(name = "auto_settle")
    val autoSettle: Boolean = true,

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    var status: MerchantStatusEntity = MerchantStatusEntity.PENDING_VERIFICATION,

    @Column(name = "email_verified_at")
    var emailVerifiedAt: Instant? = null,

    @Column(name = "created_at", updatable = false)
    val createdAt: Instant = Clock.System.now(),

    @Column(name = "updated_at")
    var updatedAt: Instant = Clock.System.now(),

    @Version
    val version: Long = 0L
)

enum class MerchantStatusEntity {
    PENDING_VERIFICATION,
    ACTIVE,
    INACTIVE,
    SUSPENDED
}