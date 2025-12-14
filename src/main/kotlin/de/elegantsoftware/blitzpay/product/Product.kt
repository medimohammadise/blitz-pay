package de.elegantsoftware.blitzpay.product

import de.elegantsoftware.blitzpay.merchant.Merchant
import jakarta.persistence.*

@Entity
@Table(name = "product")
data class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val productId: Long = 0,

    val productName: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id")
    val merchant: Merchant
)
