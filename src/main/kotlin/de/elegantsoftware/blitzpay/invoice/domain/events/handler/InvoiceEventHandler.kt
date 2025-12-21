package de.elegantsoftware.blitzpay.invoice.domain.events.handler

import de.elegantsoftware.blitzpay.invoice.domain.events.*
import org.slf4j.LoggerFactory
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Service

@Service
class InvoiceEventHandler {

    private val logger = LoggerFactory.getLogger(InvoiceEventHandler::class.java)

    @ApplicationModuleListener
    fun handleInvoiceCreated(event: InvoiceCreatedEvent) {
        logger.info(
            "Invoice created: {} for merchant {} with total {} {}",
            event.invoiceNumber, event.merchantId, event.totalAmount, event.currency
        )

        // Could trigger:
        // - Audit logging
        // - Send to analytics
        // - Update merchant dashboard
    }

    @ApplicationModuleListener
    fun handleInvoiceIssued(event: InvoiceIssuedEvent) {
        logger.info(
            "Invoice issued: {} due on {}",
            event.invoiceNumber, event.dueDate
        )

        // Could trigger:
        // - PDF generation
        // - Accounting system integration
        // - Schedule payment reminders
    }

    @ApplicationModuleListener
    fun handleInvoiceSent(event: InvoiceSentEvent) {
        logger.info(
            "Invoice sent: {} to {}",
            event.invoiceNumber, event.customerEmail
        )

        // Could trigger:
        // - Email notification to customer
        // - Update CRM
        // - Send to courier service for physical invoices
    }

    @ApplicationModuleListener
    fun handleInvoicePaid(event: InvoicePaidEvent) {
        logger.info(
            "Invoice paid: {} with {} on {} via {}",
            event.invoiceNumber, event.amountPaid, event.paymentDate, event.paymentMethod
        )

        // Could trigger:
        // - Receipt generation
//        - Update accounting
//                - Send thank you email
//                - Release digital products
    }

    @ApplicationModuleListener
    fun handleInvoiceQRCodeGenerated(event: InvoiceQRCodeGeneratedEvent) {
        logger.info(
            "QR code generated for invoice: {} amount {} {}",
            event.invoiceNumber, event.amount, event.currency
        )

        // This event will be consumed by the payment module
        // The payment module will:
        // - Generate actual QR code image
        // - Store payment reference
        // - Integrate with TrueLayer for payment processing
    }

    @ApplicationModuleListener
    fun handleInvoiceCancelled(event: InvoiceCancelledEvent) {
        logger.info(
            "Invoice cancelled: {} reason: {}",
            event.invoiceNumber, event.reason
        )

        // Could trigger:
        // - Notify sales team
//        - Update analytics
//                - Process refund if already paid
    }

    @ApplicationModuleListener
    fun handleInvoiceOverdue(event: InvoiceOverdueEvent) {
        logger.info(
            "Invoice overdue: {} is {} days overdue",
            event.invoiceNumber, event.overdueDays
        )

        // Could trigger:
        // - Send reminder email
        // - Charge late fees
        // - Notify collections department
    }
}