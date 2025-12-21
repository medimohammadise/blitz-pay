package de.elegantsoftware.blitzpay.product.outbound.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ProductRepository : JpaRepository<ProductJpaEntity, Long> {

    fun findByPublicId(publicId: UUID): ProductJpaEntity?

    fun findByMerchantIdAndPublicId(merchantId: Long, publicId: UUID): ProductJpaEntity?

    fun findAllByMerchantId(merchantId: Long, pageable: Pageable): Page<ProductJpaEntity>

    fun existsByPublicId(publicId: UUID): Boolean

    @Query("""
        SELECT p FROM ProductJpaEntity p 
        WHERE p.merchantId = :merchantId 
        AND (:query IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) 
             OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))
        AND (:status IS NULL OR p.status = :status)
        AND (:type IS NULL OR p.type = :type)
    """)
    fun searchByMerchantId(
        @Param("merchantId") merchantId: Long,
        @Param("query") query: String?,
        @Param("status") status: String?,
        @Param("type") type: String?,
        pageable: Pageable
    ): Page<ProductJpaEntity>

    @Query("""
        SELECT CASE WHEN COUNT(v) > 0 THEN TRUE ELSE FALSE END 
        FROM ProductVariantJpaEntity v 
        JOIN v.product p 
        WHERE v.sku = :sku AND p.merchantId = :merchantId
    """)
    fun existsBySkuAndMerchantId(@Param("sku") sku: String, @Param("merchantId") merchantId: Long): Boolean
}