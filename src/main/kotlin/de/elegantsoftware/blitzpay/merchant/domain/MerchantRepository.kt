package de.elegantsoftware.blitzpay.merchant.domain

import org.springframework.data.repository.Repository
import java.util.Optional
import java.util.UUID

interface MerchantRepository : Repository<Merchant, Long> {
    fun save(merchant: Merchant): Merchant
    fun findById(id: Long): Optional<Merchant>
    fun findByPublicId(publicId: UUID): Optional<Merchant>
    fun findByEmail(email: String): Optional<Merchant>
    fun existsByEmail(email: String): Boolean
}