package de.elegantsoftware.blitzpay.payment.domain

import de.elegantsoftware.blitzpay.gateways.api.GatewayType
import jakarta.persistence.*
import java.math.BigDecimal
import java.util.*
import kotlin.time.Clock
import kotlin.time.Instant

@Entity
@Table(name = "payment_links")
data class PaymentLink(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    val merchantId: UUID,

    @ElementCollection
    @CollectionTable(name = "payment_link_products", joinColumns = [JoinColumn(name = "payment_link_id")])
    @Column(name = "product_id")
    val productIds: List<UUID>,

    val amount: BigDecimal,
    val currency: String = "USD",
    val description: String,

    @Enumerated(EnumType.STRING)
    val gatewayType: GatewayType = GatewayType.TRUELAYER,

    val paymentUrl: String,
    val qrCodeUrl: String? = null,

    @Column(columnDefinition = "TEXT")
    val qrCodeData: String? = null,

    val expiresAt: Instant,
    val isActive: Boolean = true,

    @OneToOne
    @JoinColumn(name = "payment_id", referencedColumnName = "id")
    val payment: Payment? = null,

    val createdAt: Instant = Clock.System.now()
)