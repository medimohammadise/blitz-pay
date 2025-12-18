package de.elegantsoftware.blitzpay.sales.outbound.events

import de.elegantsoftware.blitzpay.sales.api.SalesEventPublisher
import de.elegantsoftware.blitzpay.sales.domain.Invoice
import de.elegantsoftware.blitzpay.sales.domain.Product
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class SpringSalesEventPublisher(
    private val eventPublisher: ApplicationEventPublisher
) : SalesEventPublisher {

    override fun publishProductCreated(product: Product) {
        eventPublisher.publishEvent(ProductCreatedEvent(product))
    }

    override fun publishProductStockUpdated(product: Product) {
        eventPublisher.publishEvent(ProductStockUpdatedEvent(product))
    }

    override fun publishInvoiceCreated(invoice: Invoice) {
        eventPublisher.publishEvent(InvoiceCreatedEvent(invoice))
    }

    override fun publishInvoiceIssued(invoice: Invoice) {
        eventPublisher.publishEvent(InvoiceIssuedEvent(invoice))
    }

    override fun publishInvoicePaid(invoice: Invoice) {
        eventPublisher.publishEvent(InvoicePaidEvent(invoice))
    }
}

// Domain Events
sealed class SalesEvent(source: Any)
data class ProductCreatedEvent(val product: Product) : SalesEvent(product)
data class ProductStockUpdatedEvent(val product: Product) : SalesEvent(product)
data class InvoiceCreatedEvent(val invoice: Invoice) : SalesEvent(invoice)
data class InvoiceIssuedEvent(val invoice: Invoice) : SalesEvent(invoice)
data class InvoicePaidEvent(val invoice: Invoice) : SalesEvent(invoice)