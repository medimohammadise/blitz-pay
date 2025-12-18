package de.elegantsoftware.blitzpay.sales.api

import de.elegantsoftware.blitzpay.sales.domain.Invoice
import de.elegantsoftware.blitzpay.sales.domain.Product

interface SalesEventPublisher {
    fun publishProductCreated(product: Product)
    fun publishProductStockUpdated(product: Product)
    fun publishInvoiceCreated(invoice: Invoice)
    fun publishInvoiceIssued(invoice: Invoice)
    fun publishInvoicePaid(invoice: Invoice)
}