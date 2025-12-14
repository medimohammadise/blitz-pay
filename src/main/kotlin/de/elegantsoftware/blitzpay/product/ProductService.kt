package de.elegantsoftware.blitzpay.product

import de.elegantsoftware.blitzpay.merchant.MerchantRepository
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val merchantRepository: MerchantRepository
) {

    fun create(productName: String, merchantId: Long): Product {
        val merchant = merchantRepository.findById(merchantId).orElseThrow()
        val product = Product(productName = productName, merchant = merchant)
        return productRepository.save(product)
    }

    fun findAll(): List<Product> = productRepository.findAll()

    fun findById(id: Long): Product = productRepository.findById(id).orElseThrow()

    fun delete(id: Long) = productRepository.deleteById(id)
}
