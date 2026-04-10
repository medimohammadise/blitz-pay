package com.elegant.software.blitzpay.invoice.internal

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant
import java.util.LinkedHashSet
import java.util.UUID

@Entity
@Table(name = "invoice_generation_activities")
class InvoiceGenerationActivity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "invoice_number", nullable = false, unique = true, updatable = false)
    val invoiceNumber: String,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "amount_minor_units", nullable = false)
    val amountMinorUnits: Long,

    @Column(name = "currency", nullable = false, length = 3)
    val currency: String = "EUR",

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "invoice_generation_activity_recipients",
        joinColumns = [JoinColumn(name = "activity_id")]
    )
    val recipients: MutableSet<InvoiceRecipient> = LinkedHashSet(),

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    var paymentStatus: PaymentStatus = PaymentStatus.PENDING
) {
    init {
        require(invoiceNumber.isNotBlank()) { "invoiceNumber must not be blank" }
        require(currency.length == 3) { "currency must use a 3-letter ISO code" }
        require(amountMinorUnits >= 0) { "amountMinorUnits must not be negative" }
        require(recipients.isNotEmpty()) { "at least one invoice recipient is required" }
    }

    fun markPaid() {
        paymentStatus = PaymentStatus.PAID
    }

    fun markReceived() {
        paymentStatus = PaymentStatus.RECEIVED
    }

    fun amount(): BigDecimal = BigDecimal.valueOf(amountMinorUnits, 2)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is InvoiceGenerationActivity) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
