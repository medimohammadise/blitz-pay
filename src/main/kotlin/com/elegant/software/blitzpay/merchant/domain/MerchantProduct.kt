package com.elegant.software.blitzpay.merchant.domain

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OrderColumn
import jakarta.persistence.Table
import org.hibernate.annotations.Filter
import org.hibernate.annotations.FilterDef
import org.hibernate.annotations.ParamDef
import org.hibernate.type.descriptor.java.UUIDJavaType
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@FilterDef(
    name = "tenantFilter",
    parameters = [ParamDef(name = "merchantId", type = UUIDJavaType::class)]
)
@Filter(name = "tenantFilter", condition = "merchant_application_id = :merchantId")
@Entity
@Table(name = "merchant_products", schema = "blitzpay")
class MerchantProduct(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "merchant_application_id", nullable = false, updatable = false)
    val merchantApplicationId: UUID,

    @Column(nullable = false)
    var name: String,

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 4)
    var unitPrice: BigDecimal,

    @Column(nullable = false)
    var active: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = createdAt
) {
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "merchant_product_images",
        joinColumns = [JoinColumn(name = "product_id")]
    )
    @OrderColumn(name = "display_order")
    @Column(name = "storage_key", length = 512)
    var images: MutableList<String> = mutableListOf()
    fun deactivate(at: Instant = Instant.now()) {
        active = false
        updatedAt = at
    }

    fun update(name: String, unitPrice: BigDecimal, imageUrls: List<String>, at: Instant = Instant.now()) {
        require(unitPrice >= BigDecimal.ZERO) { "unitPrice must be >= 0" }
        this.name = name
        this.unitPrice = unitPrice
        this.images = imageUrls.toMutableList()
        this.updatedAt = at
    }
}
