package de.elegantsoftware.blitzpay.product.outbound.persistence

import de.elegantsoftware.blitzpay.product.domain.Price
import de.elegantsoftware.blitzpay.product.domain.ProductVariant
import jakarta.persistence.*
import java.math.BigDecimal
import java.util.*

@Entity
@Table(
    name = "product_variants",
    indexes = [
        Index(name = "idx_variant_sku", columnList = "sku"),
        Index(name = "idx_variant_product", columnList = "product_id")
    ]
)
data class ProductVariantJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, updatable = false)
    val variantId: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: ProductJpaEntity? = null,

    @Column(nullable = false)
    val sku: String,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, precision = 19, scale = 4)
    var priceAmount: BigDecimal,

    @Column(nullable = false, length = 3)
    var priceCurrency: String,

    @Column(nullable = false)
    var priceTaxInclusive: Boolean = false,

    @ElementCollection
    @CollectionTable(
        name = "product_variant_attributes",
        joinColumns = [JoinColumn(name = "variant_id")]
    )
    @MapKeyColumn(name = "attribute_key")
    @Column(name = "attribute_value")
    val attributes: MutableMap<String, String> = mutableMapOf()
) {
    fun toDomain(): ProductVariant {
        return ProductVariant(
            id = variantId,
            sku = sku,
            name = name,
            price = Price(
                amount = priceAmount,
                currency = priceCurrency,
                taxInclusive = priceTaxInclusive
            ),
            attributes = attributes.toMap()
        )
    }

    companion object {
        fun fromDomain(variant: ProductVariant, product: ProductJpaEntity? = null): ProductVariantJpaEntity {
            val entity = ProductVariantJpaEntity(
                variantId = variant.id,
                product = product,
                sku = variant.sku,
                name = variant.name,
                priceAmount = variant.price.amount,
                priceCurrency = variant.price.currency,
                priceTaxInclusive = variant.price.taxInclusive
            )
            entity.attributes.putAll(variant.attributes)
            return entity
        }
    }
}