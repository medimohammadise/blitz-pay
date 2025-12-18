package de.elegantsoftware.blitzpay.sales.api

import de.elegantsoftware.blitzpay.sales.inbound.web.dto.*
import java.util.UUID

interface InvoiceServicePort {
    fun createInvoice(request: CreateInvoiceRequest): InvoiceResponse
    fun getInvoice(id: UUID): InvoiceResponse
    fun getInvoiceByNumber(invoiceNumber: String): InvoiceResponse
    fun addInvoiceItem(invoiceId: UUID, request: AddInvoiceItemRequest): InvoiceResponse
    fun issueInvoice(id: UUID): InvoiceResponse
    fun markInvoiceAsPaid(id: UUID, request: MarkInvoicePaidRequest): InvoiceResponse
    fun cancelInvoice(id: UUID, reason: String?): InvoiceResponse
    fun getMerchantInvoices(merchantId: UUID): List<InvoiceResponse>
}