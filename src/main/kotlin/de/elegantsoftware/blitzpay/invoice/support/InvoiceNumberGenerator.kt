package de.elegantsoftware.blitzpay.invoice.support

import de.elegantsoftware.blitzpay.invoice.outbound.persistence.InvoiceRepository
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class InvoiceNumberGenerator(
    private val invoiceRepository: InvoiceRepository
) {
    
    fun generate(merchantId: Long): String {
        val year = LocalDate.now().year.toString()
        val month = String.format("%02d", LocalDate.now().monthValue)
        
        val prefix = "INV-$year$month-"
        val lastNumber = invoiceRepository.findLastInvoiceNumberByMerchant(merchantId, prefix) ?: 0L
        
        val sequence = lastNumber + 1
        return "$prefix${String.format("%05d", sequence)}"
    }
}