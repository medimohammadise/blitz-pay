package de.elegantsoftware.blitzpay.product.outbound.persistence

import de.elegantsoftware.blitzpay.product.domain.*
import jakarta.persistence.*
import java.math.BigDecimal
import java.util.*
import kotlin.time.Clock
import kotlin.time.Instant

@Entity
@Table(
    name = "products",
    indexes = [
        Index(name = "idx_product_merchant_id", columnList = "merchantId"),
        Index(name = "idx_product_public_id", columnList = "publicId", unique = true),
        Index(name = "idx_product_status", columnList = "status"),
        Index(name = "idx_product_category", columnList = "categoryId")
    ]
)
data class ProductJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true, nullable = false, updatable = false)
    val publicId: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val merchantId: Long,

    @Column(nullable = false)
    var name: String,

    @Column(columnDefinition = "TEXT")
    var description: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: ProductType,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ProductStatus,

    @Column(nullable = false, precision = 19, scale = 4)
    var basePriceAmount: BigDecimal,

    @Column(nullable = false, length = 3)
    var basePriceCurrency: String,

    @Column(nullable = false)
    var basePriceTaxInclusive: Boolean = false,

    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    val variants: MutableList<ProductVariantJpaEntity> = mutableListOf(),

    @Embedded
    var inventory: ProductInventoryEmbeddable? = null,

    @Column
    var categoryId: Long? = null,

    @ElementCollection
    @CollectionTable(name = "product_tags", joinColumns = [JoinColumn(name = "product_id")])
    @Column(name = "tag")
    val tags: MutableSet<String> = mutableSetOf(),

    @ElementCollection
    @CollectionTable(name = "product_metadata", joinColumns = [JoinColumn(name = "product_id")])
    @MapKeyColumn(name = "meta_key")
    @Column(name = "meta_value")
    val metadata: MutableMap<String, String> = mutableMapOf(),

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Clock.System.now(),

    @Column(nullable = false)
    var updatedAt: Instant = Clock.System.now()
) {
    fun toDomain(): Product {
        return Product(
            id = ProductId(id!!),
            publicId = publicId,
            merchantId = merchantId,
            name = name,
            description = description,
            type = type,
            status = status,
            basePrice = Price(
                amount = basePriceAmount,
                currency = basePriceCurrency,
                taxInclusive = basePriceTaxInclusive
            ),
            variants = variants.map { it.toDomain() },
            inventory = inventory?.toDomain() ?: ProductInventory(),
            categoryId = categoryId,
            tags = tags.toSet(),
            metadata = metadata.toMap(),
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(product: Product): ProductJpaEntity {
            val entity = ProductJpaEntity(
                id = if (product.id.value == 0L) null else product.id.value,
                publicId = product.publicId,
                merchantId = product.merchantId,
                name = product.name,
                description = product.description,
                type = product.type,
                status = product.status,
                basePriceAmount = product.basePrice.amount,
                basePriceCurrency = product.basePrice.currency,
                basePriceTaxInclusive = product.basePrice.taxInclusive,
                inventory = ProductInventoryEmbeddable.fromDomain(product.inventory),
                categoryId = product.categoryId,
                createdAt = product.createdAt,
                updatedAt = product.updatedAt
            )

            entity.tags.addAll(product.tags)
            entity.metadata.putAll(product.metadata)

            product.variants.forEach { variant ->
                entity.variants.add(ProductVariantJpaEntity.fromDomain(variant, entity))
            }

            return entity
        }
    }
}

@Embeddable
data class ProductInventoryEmbeddable(
    @Column
    val quantity: Int = 0,

    @Column
    val lowStockThreshold: Int = 10,

    @Column
    val isTracked: Boolean = true
) {
    fun toDomain(): ProductInventory {
        return ProductInventory(
            quantity = quantity,
            lowStockThreshold = lowStockThreshold,
            isTracked = isTracked
        )
    }

    companion object {
        fun fromDomain(inventory: ProductInventory): ProductInventoryEmbeddable {
            return ProductInventoryEmbeddable(
                quantity = inventory.quantity,
                lowStockThreshold = inventory.lowStockThreshold,
                isTracked = inventory.isTracked
            )
        }
    }
}