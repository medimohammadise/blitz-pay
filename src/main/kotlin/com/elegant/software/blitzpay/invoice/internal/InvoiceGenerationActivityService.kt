package com.elegant.software.blitzpay.invoice.internal

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.util.LinkedHashSet
import java.util.UUID

@Service
class InvoiceGenerationActivityService(
    private val invoiceGenerationActivityRepository: InvoiceGenerationActivityRepository
) {

    @Transactional
    fun recordActivity(
        invoiceNumber: String,
        amount: BigDecimal,
        recipients: List<InvoiceRecipient>,
        createdAt: Instant = Instant.now(),
        currency: String = "EUR",
        paymentStatus: PaymentStatus = PaymentStatus.PENDING
    ): InvoiceGenerationActivity {
        val activity = InvoiceGenerationActivity(
            invoiceNumber = invoiceNumber,
            createdAt = createdAt,
            amountMinorUnits = amount.toMinorUnits(),
            currency = currency,
            recipients = LinkedHashSet(recipients),
            paymentStatus = paymentStatus
        )

        return invoiceGenerationActivityRepository.save(activity)
    }

    @Transactional
    fun updatePaymentStatus(activityId: UUID, paymentStatus: PaymentStatus): InvoiceGenerationActivity {
        val activity = invoiceGenerationActivityRepository.findById(activityId)
            .orElseThrow { InvoiceGenerationActivityNotFoundException(activityId) }

        activity.paymentStatus = paymentStatus
        return activity
    }

    @Transactional(readOnly = true)
    fun findByPaymentStatus(paymentStatus: PaymentStatus): List<InvoiceGenerationActivity> =
        invoiceGenerationActivityRepository.findAllByPaymentStatusOrderByCreatedAtDesc(paymentStatus)

    private fun BigDecimal.toMinorUnits(): Long =
        movePointRight(2)
            .setScale(0, RoundingMode.HALF_UP)
            .longValueExact()
}

class InvoiceGenerationActivityNotFoundException(activityId: UUID) :
    NoSuchElementException("Invoice generation activity not found: $activityId")
