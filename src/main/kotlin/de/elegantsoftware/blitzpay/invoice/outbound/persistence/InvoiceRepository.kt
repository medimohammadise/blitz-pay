package de.elegantsoftware.blitzpay.invoice.outbound.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.util.UUID
interface InvoiceRepository : JpaRepository<InvoiceEntity, Long> {
    fun findByUuid(uuid: UUID): InvoiceEntity?
    fun findByInvoiceNumber(invoiceNumber: String): InvoiceEntity?
    fun findByMerchantId(merchantId: Long, pageable: Pageable): Page<InvoiceEntity>
    fun findByMerchantIdAndCustomerId(merchantId: Long, customerId: Long): List<InvoiceEntity>
    fun findByMerchantIdAndStatus(merchantId: Long, status: String): List<InvoiceEntity>
    
    @Query("""
        SELECT i FROM InvoiceEntity i 
        WHERE i.merchantId = :merchantId 
        AND i.status IN ('ISSUED', 'SENT', 'PARTIALLY_PAID') 
        AND i.dueDate < :date
    """)
    fun findOverdueInvoicesByMerchant(
        @Param("merchantId") merchantId: Long,
        @Param("date") date: LocalDate
    ): List<InvoiceEntity>
    
    @Query("""
        SELECT i FROM InvoiceEntity i 
        WHERE i.status IN ('ISSUED', 'SENT', 'PARTIALLY_PAID') 
        AND i.dueDate < :date
    """)
    fun findAllOverdueInvoices(@Param("date") date: LocalDate): List<InvoiceEntity>

    @Query(
        value = """
            SELECT MAX(CAST(SUBSTRING(i.invoice_number, 6) AS BIGINT))
            FROM invoices i 
            WHERE i.merchant_id = :merchantId 
            AND i.invoice_number LIKE :prefix || '%'
        """,
        nativeQuery = true
    )
    fun findLastInvoiceNumberByMerchant(
        @Param("merchantId") merchantId: Long,
        @Param("prefix") prefix: String
    ): Long?
}