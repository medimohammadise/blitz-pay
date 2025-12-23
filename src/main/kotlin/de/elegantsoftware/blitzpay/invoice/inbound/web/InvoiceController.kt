package de.elegantsoftware.blitzpay.invoice.inbound.web

import de.elegantsoftware.blitzpay.invoice.api.InvoiceService
import de.elegantsoftware.blitzpay.invoice.api.RemoveInvoiceItemApiRequest
import de.elegantsoftware.blitzpay.invoice.inbound.web.dto.*
import de.elegantsoftware.blitzpay.invoice.support.mapper.InvoiceMapper
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/invoices")
@Tag(name = "Invoice Management", description = "APIs for invoice creation, management, and processing")
class InvoiceController(
    private val invoiceService: InvoiceService,
    private val invoiceMapper: InvoiceMapper
) {
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create invoice", description = "Creates a new invoice for products with customer information and payment tracking")
    fun createInvoice(@RequestBody request: CreateInvoiceWebRequest): InvoiceWebResponse {
        val apiRequest = invoiceMapper.toApiRequest(request)
        val invoice = invoiceService.createInvoice(apiRequest)
        return invoiceMapper.toWebResponse(invoice)
    }
    
    @GetMapping("/{id}")
    fun getInvoice(@PathVariable id: Long): InvoiceWebResponse {
        val invoice = invoiceService.getInvoiceById(id)
            ?: throw ResourceNotFoundException("Invoice with id $id not found")
        return invoiceMapper.toWebResponse(invoice)
    }
    
    @GetMapping("/uuid/{uuid}")
    fun getInvoiceByUuid(@PathVariable uuid: UUID): InvoiceWebResponse {
        val invoice = invoiceService.getInvoiceByUuid(uuid)
            ?: throw ResourceNotFoundException("Invoice with uuid $uuid not found")
        return invoiceMapper.toWebResponse(invoice)
    }
    
    @GetMapping("/merchant/{merchantId}")
    fun getMerchantInvoices(
        @PathVariable merchantId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): List<InvoiceWebResponse> {
        return invoiceService.getInvoicesByMerchant(merchantId, page, size)
            .map { invoiceMapper.toWebResponse(it) }
    }
    
    @GetMapping("/merchant/{merchantId}/customer/{customerId}")
    fun getCustomerInvoices(
        @PathVariable merchantId: Long,
        @PathVariable customerId: Long
    ): List<InvoiceWebResponse> {
        return invoiceService.getInvoicesByCustomer(customerId, merchantId)
            .map { invoiceMapper.toWebResponse(it) }
    }
    
    @PutMapping("/{id}")
    fun updateInvoice(
        @PathVariable id: Long,
        @RequestBody request: UpdateInvoiceWebRequest
    ): InvoiceWebResponse {
        val apiRequest = invoiceMapper.toApiRequest(request, id)
        val invoice = invoiceService.updateInvoice(apiRequest)
        return invoiceMapper.toWebResponse(invoice)
    }
    
    @PostMapping("/{id}/items")
    fun addInvoiceItem(
        @PathVariable id: Long,
        @RequestBody request: AddInvoiceItemWebRequest
    ): InvoiceWebResponse {
        val apiRequest = invoiceMapper.toApiRequest(request, id)
        val invoice = invoiceService.addItem(apiRequest)
        return invoiceMapper.toWebResponse(invoice)
    }
    
    @DeleteMapping("/{id}/items/{productId}")
    fun removeInvoiceItem(
        @PathVariable id: Long,
        @PathVariable productId: Long
    ): InvoiceWebResponse {
        val apiRequest = RemoveInvoiceItemApiRequest(
            invoiceId = id,
            productId = productId
        )
        val invoice = invoiceService.removeItem(apiRequest)
        return invoiceMapper.toWebResponse(invoice)
    }
    
    @PostMapping("/{id}/issue")
    fun issueInvoice(@PathVariable id: Long): InvoiceWebResponse {
        val invoice = invoiceService.issueInvoice(id)
        return invoiceMapper.toWebResponse(invoice)
    }
    
    @PostMapping("/{id}/send")
    fun sendInvoice(@PathVariable id: Long): InvoiceWebResponse {
        val invoice = invoiceService.sendInvoice(id)
        return invoiceMapper.toWebResponse(invoice)
    }
    
    @PostMapping("/{id}/pay")
    fun payInvoice(
        @PathVariable id: Long,
        @RequestBody request: PaymentWebRequest
    ): InvoiceWebResponse {
        val apiRequest = invoiceMapper.toPaymentDetailsApi(request)
        val invoice = invoiceService.markAsPaid(id, apiRequest)
        return invoiceMapper.toWebResponse(invoice)
    }
    
    @PostMapping("/{id}/cancel")
    fun cancelInvoice(
        @PathVariable id: Long,
        @RequestBody request: CancelInvoiceWebRequest
    ): InvoiceWebResponse {
        val invoice = invoiceService.cancelInvoice(id, request.reason)
        return invoiceMapper.toWebResponse(invoice)
    }
    
    @PostMapping("/{id}/qr-code")
    fun generateQRCode(@PathVariable id: Long): QRCodeWebResponse {
        val qrCodeData = invoiceService.generateQRCode(id)
        return QRCodeWebResponse(qrCodeData = qrCodeData)
    }
    
    @GetMapping("/{id}/tax-summary")
    fun getTaxSummary(@PathVariable id: Long): TaxSummaryWebResponse {
        val summary = invoiceService.calculateTaxSummary(id)
        return TaxSummaryWebResponse(
            taxableAmount = summary.taxableAmount,
            taxAmount = summary.taxAmount,
            taxRates = summary.taxRates
        )
    }
    
    @GetMapping("/{id}/pdf", produces = [MediaType.APPLICATION_PDF_VALUE])
    fun getInvoicePdf(@PathVariable id: Long): ResponseEntity<ByteArray> {
        val pdfContent = invoiceService.generateInvoicePdf(id)
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=invoice-$id.pdf")
            .body(pdfContent)
    }
    
    @GetMapping("/overdue")
    fun getOverdueInvoices(
        @RequestParam(required = false) merchantId: Long?
    ): List<InvoiceWebResponse> {
        return invoiceService.findOverdueInvoices(merchantId)
            .map { invoiceMapper.toWebResponse(it) }
    }
}

@ResponseStatus(HttpStatus.NOT_FOUND)
class ResourceNotFoundException(message: String) : RuntimeException(message)