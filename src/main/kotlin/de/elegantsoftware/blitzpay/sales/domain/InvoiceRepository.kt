package de.elegantsoftware.blitzpay.sales.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface InvoiceRepository : JpaRepository<Invoice, Long> {

    // Custom queries with @Query annotations
    @Query("SELECT i FROM Invoice i WHERE i.publicId = :publicId")
    fun findByPublicId(publicId: UUID): Invoice?

    fun findByInvoiceNumber(invoiceNumber: String): Invoice?

    @Query("SELECT i FROM Invoice i WHERE i.merchantInfo.id = :merchantId")
    fun findAllByMerchantId(@Param("merchantId") merchantId: UUID): List<Invoice>

    fun findAllByStatus(status: InvoiceStatus): List<Invoice>

    fun findAllByIssueDateBetween(startDate: LocalDate, endDate: LocalDate): List<Invoice>

    fun findAllByDueDateBeforeAndStatus(date: LocalDate, status: InvoiceStatus): List<Invoice>

}