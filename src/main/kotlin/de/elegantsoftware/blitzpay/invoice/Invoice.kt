package de.elegantsoftware.blitzpay.invoice

import de.elegantsoftware.blitzpay.merchant.Merchant
import jakarta.persistence.*

@Entity
@Table(name = "invoice")
data class Invoice(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id")
    val merchant: Merchant,

    @OneToMany(mappedBy = "invoice", cascade = [CascadeType.ALL], orphanRemoval = true)
    val items: List<InvoiceItem> = emptyList()
)
