package de.elegantsoftware.blitzpay.merchant.outbound.persistence

import de.elegantsoftware.blitzpay.merchant.domain.MerchantId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MerchantRepository : JpaRepository<MerchantJpaEntity, Long> {

    fun findByPublicId(publicId: UUID): MerchantJpaEntity?

    fun findByEmail(email: String): MerchantJpaEntity?

    fun existsByEmail(email: String): Boolean

    @Query("SELECT m FROM MerchantJpaEntity m WHERE m.status = :status")
    fun findByStatus(@Param("status") status: String): List<MerchantJpaEntity>

    // Custom query to find by internal ID
    fun findById(merchantId: MerchantId): MerchantJpaEntity? =
        findById(merchantId.value).orElse(null)
}