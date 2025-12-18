package de.elegantsoftware.blitzpay.sales.inbound.web

import de.elegantsoftware.blitzpay.sales.api.InvoiceServicePort
import de.elegantsoftware.blitzpay.sales.inbound.web.dto.*
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/invoices")
@Tag(
    name = "Invoices",
    description = "Invoice management APIs"
)
class InvoiceController(
    private val invoiceService: InvoiceServicePort
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createInvoice(@Valid @RequestBody request: CreateInvoiceRequest): InvoiceResponse {
        return invoiceService.createInvoice(request)
    }

    @GetMapping("/{id}")
    fun getInvoice(@PathVariable id: UUID): InvoiceResponse {
        return invoiceService.getInvoice(id)
    }

    @GetMapping("/number/{invoiceNumber}")
    fun getInvoiceByNumber(@PathVariable invoiceNumber: String): InvoiceResponse {
        return invoiceService.getInvoiceByNumber(invoiceNumber)
    }

    @PostMapping("/{id}/items")
    fun addInvoiceItem(
        @PathVariable id: UUID,
        @Valid @RequestBody request: AddInvoiceItemRequest
    ): InvoiceResponse {
        return invoiceService.addInvoiceItem(id, request)
    }

    @PostMapping("/{id}/issue")
    fun issueInvoice(@PathVariable id: UUID): InvoiceResponse {
        return invoiceService.issueInvoice(id)
    }

    @PostMapping("/{id}/pay")
    fun markInvoiceAsPaid(
        @PathVariable id: UUID,
        @Valid @RequestBody request: MarkInvoicePaidRequest
    ): InvoiceResponse {
        return invoiceService.markInvoiceAsPaid(id, request)
    }

    @PostMapping("/{id}/cancel")
    fun cancelInvoice(
        @PathVariable id: UUID,
        @RequestParam(required = false) reason: String?
    ): InvoiceResponse {
        return invoiceService.cancelInvoice(id, reason)
    }

    @GetMapping("/merchant/{merchantId}")
    fun getMerchantInvoices(@PathVariable merchantId: UUID): List<InvoiceResponse> {
        return invoiceService.getMerchantInvoices(merchantId)
    }
}