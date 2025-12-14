package de.elegantsoftware.blitzpay.invoice

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/invoices")
class InvoiceController(private val invoiceService: InvoiceService) {

    @PostMapping
    fun create(@RequestBody request: CreateInvoiceRequest): ResponseEntity<Invoice> =
        ResponseEntity.ok(invoiceService.create(request))

    @GetMapping
    fun getAll(): ResponseEntity<List<Invoice>> =
        ResponseEntity.ok(invoiceService.findAll())

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<Invoice> =
        ResponseEntity.ok(invoiceService.findById(id))

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        invoiceService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
