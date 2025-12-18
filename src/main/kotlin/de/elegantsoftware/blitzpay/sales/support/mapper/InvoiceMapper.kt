package de.elegantsoftware.blitzpay.sales.support.mapper

import de.elegantsoftware.blitzpay.sales.domain.Invoice
import de.elegantsoftware.blitzpay.sales.domain.InvoiceStatus
import de.elegantsoftware.blitzpay.sales.inbound.web.dto.*
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class InvoiceMapper {

    fun toResponse(invoice: Invoice): InvoiceResponse {
        return InvoiceResponse(
            id = invoice.publicId,
            invoiceNumber = invoice.invoiceNumber,
            merchant = MerchantResponse(
                id = invoice.merchantInfo.id,
                name = invoice.merchantInfo.name,
                email = invoice.merchantInfo.email
            ),
            items = invoice.items.map { item ->
                InvoiceItemResponse(
                    id = item.publicId,
                    product = ProductRefResponse(
                        id = item.productInfo.id,
                        sku = item.productInfo.sku,
                        name = item.productInfo.name
                    ),
                    quantity = item.quantity,
                    unitPrice = item.unitPrice.amount,
                    currency = item.unitPrice.currency,
                    lineTotal = item.lineTotal.amount,
                    taxAmount = item.taxAmount
                )
            },
            status = invoice.status.name,
            issueDate = invoice.issueDate,
            dueDate = invoice.dueDate,
            billingAddress = invoice.billingAddress?.let { address ->
                BillingAddressResponse(
                    street = address.street,
                    city = address.city,
                    postalCode = address.postalCode,
                    country = address.country
                )
            },
            notes = invoice.notes,
            totals = InvoiceTotalsResponse(
                subtotal = invoice.totals.subtotalAmount.amount,
                tax = invoice.totals.taxAmount.amount,
                total = invoice.totals.totalAmount.amount,
                paid = invoice.totals.paidAmount.amount,
                currency = invoice.totals.totalAmount.currency,
                paymentDate = invoice.totals.paymentDate,
                isPaid = invoice.status == InvoiceStatus.PAID,
                isOverdue = invoice.status == InvoiceStatus.ISSUED &&
                        invoice.dueDate.isBefore(LocalDate.now())
            ),
            createdAt = invoice.createdAt,
            updatedAt = invoice.updatedAt
        )
    }
}