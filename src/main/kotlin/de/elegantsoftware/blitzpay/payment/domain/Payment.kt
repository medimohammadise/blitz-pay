package de.elegantsoftware.blitzpay.payment.domain

import de.elegantsoftware.blitzpay.gateways.api.PaymentStatus
import de.elegantsoftware.blitzpay.gateways.api.GatewayType
import jakarta.persistence.*
import java.math.BigDecimal
import java.util.*
import kotlin.time.Clock
import kotlin.time.Instant

@Entity
@Table(name = "payments")
data class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    val merchantId: UUID,

    @ElementCollection
    @CollectionTable(name = "payment_products", joinColumns = [JoinColumn(name = "payment_id")])
    @Column(name = "product_id")
    val productIds: List<UUID> = emptyList(),

    val amount: BigDecimal,
    val currency: String = "USD",

    @Enumerated(EnumType.STRING)
    val gatewayType: GatewayType = GatewayType.TRUELAYER,

    val gatewayPaymentId: String? = null,
    val paymentLinkId: UUID? = null,

    @Enumerated(EnumType.STRING)
    var status: PaymentStatus = PaymentStatus.PENDING,

    val customerEmail: String? = null,
    val description: String = "",

    @Column(columnDefinition = "TEXT")
    val qrCodeData: String? = null,

    val createdAt: Instant = Clock.System.now(),
    var updatedAt: Instant = Clock.System.now(),
    var paidAt: Instant? = null
)


