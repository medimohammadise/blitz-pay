package de.elegantsoftware.blitzpay.sales.domain

import de.elegantsoftware.blitzpay.common.domain.BaseEntity
import de.elegantsoftware.blitzpay.merchant.domain.Merchant
import jakarta.persistence.*
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(
    name = "products",
    indexes = [
        Index(name = "idx_product_public_id", columnList = "public_id", unique = true),
        Index(name = "idx_product_merchant_sku", columnList = "merchant_id, sku", unique = true),
        Index(name = "idx_product_merchant", columnList = "merchant_id"),
        Index(name = "idx_product_status", columnList = "status")
    ]
)
class Product(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false, referencedColumnName = "id")
    val merchant: Merchant,

    @Column(nullable = false, unique = true)
    val sku: String,

    @Column(nullable = false)
    var name: String,

    @Column(length = 2000)
    var description: String? = null,

    @Embedded
    var price: Money,

    @Column(nullable = false)
    var stockQuantity: Int = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ProductStatus = ProductStatus.ACTIVE,

    @ElementCollection
    @CollectionTable(
        name = "product_categories",
        joinColumns = [JoinColumn(name = "product_id", referencedColumnName = "id")]
    )
    @Column(name = "category", length = 50)
    var categories: MutableSet<String> = mutableSetOf(),

    @Embedded
    var taxInfo: TaxInfo? = null

) : BaseEntity() {

    companion object {
        fun create(
            merchant: Merchant,
            sku: String,
            name: String,
            price: Money,
            initialStock: Int = 0
        ): Product {
            require(sku.isNotBlank()) { "SKU cannot be blank" }
            require(name.isNotBlank()) { "Product name cannot be blank" }
            require(initialStock >= 0) { "Initial stock cannot be negative" }
            require(price.amount > BigDecimal.ZERO) { "Price must be positive" }
//            For now, we allow to create product
//            require(merchant.status == de.elegantsoftware.blitzpay.merchant.domain.MerchantStatus.ACTIVE) {
//                "Merchant must be active to create products"
//            }

            return Product(
                merchant = merchant,
                sku = sku.trim(),
                name = name.trim(),
                price = price,
                stockQuantity = initialStock
            )
        }
    }

    fun updateStock(quantity: Int): Int {
        val newStock = stockQuantity + quantity
        require(newStock >= 0) { "Insufficient stock. Available: $stockQuantity, requested: ${-quantity}" }

        stockQuantity = newStock
        return stockQuantity
    }

    fun deactivate() {
        status = ProductStatus.INACTIVE
    }

    fun activate() {
        status = ProductStatus.ACTIVE
    }

    fun updatePrice(newPrice: Money) {
        require(newPrice.amount > BigDecimal.ZERO) { "Price must be positive" }
        price = newPrice
    }

    fun isAvailable(): Boolean {
        return status == ProductStatus.ACTIVE && stockQuantity > 0
    }

    fun belongsToMerchant(merchantId: UUID): Boolean {
        return merchant.publicId == merchantId
    }
}

// Value Objects remain the same
@Embeddable
data class Money(
    @Column(nullable = false, precision = 10, scale = 2)
    val amount: BigDecimal,

    @Column(nullable = false, length = 3)
    val currency: String = "EUR"
) {
    init {
        require(amount >= BigDecimal.ZERO) { "Amount cannot be negative" }
        require(currency.length == 3) { "Currency must be 3 characters" }
    }

    operator fun times(quantity: Int): Money {
        return this.copy(amount = amount.multiply(BigDecimal(quantity)))
    }
}

@Embeddable
data class TaxInfo(
    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 3)
    val rate: BigDecimal, // e.g., 0.19 for 19%

    @Column(name = "tax_code", length = 20)
    val code: String? = null
) {
    init {
        require(rate >= BigDecimal.ZERO && rate <= BigDecimal.ONE) {
            "Tax rate must be between 0 and 1"
        }
    }

    fun calculateTax(amount: BigDecimal): BigDecimal {
        return amount.multiply(rate)
    }
}

enum class ProductStatus {
    ACTIVE, INACTIVE, DISCONTINUED, OUT_OF_STOCK
}