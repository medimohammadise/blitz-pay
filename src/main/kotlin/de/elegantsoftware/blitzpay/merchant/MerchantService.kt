package de.elegantsoftware.blitzpay.merchant

import org.springframework.stereotype.Service

@Service
class MerchantService(private val merchantRepository: MerchantRepository) {

    fun create(merchant: Merchant): Merchant = merchantRepository.save(merchant)

    fun findAll(): List<Merchant> = merchantRepository.findAll()

    fun findById(id: Long): Merchant = merchantRepository.findById(id).orElseThrow()

    fun delete(id: Long) = merchantRepository.deleteById(id)
}
