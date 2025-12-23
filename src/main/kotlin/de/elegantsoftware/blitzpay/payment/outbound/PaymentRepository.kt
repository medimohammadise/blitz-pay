package de.elegantsoftware.blitzpay.payment.outbound

import de.elegantsoftware.blitzpay.gateways.api.PaymentStatus
import de.elegantsoftware.blitzpay.payment.domain.Payment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PaymentRepository : JpaRepository<Payment, UUID> {
    
    fun findByGatewayPaymentId(gatewayPaymentId: String): Payment?
    
    fun findByMerchantId(merchantId: UUID): List<Payment>
    
    fun findByMerchantIdAndStatus(merchantId: UUID, status: PaymentStatus): List<Payment>
    
    @Query("SELECT p FROM Payment p WHERE p.paymentLinkId = :paymentLinkId")
    fun findByPaymentLinkId(@Param("paymentLinkId") paymentLinkId: UUID): Payment?
    
    fun findByCustomerEmail(customerEmail: String): List<Payment>
    
    @Query("SELECT p FROM Payment p WHERE p.createdAt >= :startDate AND p.createdAt <= :endDate")
    fun findPaymentsBetweenDates(
        @Param("startDate") startDate: java.time.LocalDateTime,
        @Param("endDate") endDate: java.time.LocalDateTime
    ): List<Payment>
}