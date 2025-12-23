package de.elegantsoftware.blitzpay.payment.outbound

import de.elegantsoftware.blitzpay.payment.domain.PaymentLink
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface PaymentLinkRepository : JpaRepository<PaymentLink, UUID> {
    
    fun findByMerchantId(merchantId: UUID): List<PaymentLink>
    
    fun findByMerchantIdAndIsActive(merchantId: UUID, isActive: Boolean): List<PaymentLink>
    
    @Query("SELECT pl FROM PaymentLink pl WHERE pl.expiresAt < :now AND pl.isActive = true")
    fun findExpiredActiveLinks(@Param("now") now: LocalDateTime): List<PaymentLink>
    
    @Query("SELECT pl FROM PaymentLink pl WHERE pl.paymentUrl = :paymentUrl")
    fun findByPaymentUrl(@Param("paymentUrl") paymentUrl: String): PaymentLink?
    
    fun findByPaymentIdIn(paymentIds: List<UUID>): List<PaymentLink>
}