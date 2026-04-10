package com.elegant.software.blitzpay.invoice.internal

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface InvoiceGenerationActivityRepository : JpaRepository<InvoiceGenerationActivity, UUID> {
    fun findAllByPaymentStatusOrderByCreatedAtDesc(paymentStatus: PaymentStatus): List<InvoiceGenerationActivity>
}
