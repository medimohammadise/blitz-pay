package de.elegantsoftware.blitzpay.invoice.outbound.persistence

import de.elegantsoftware.blitzpay.invoice.domain.InvoiceItem
import jakarta.persistence.*
import java.math.BigDecimal
import java.util.UUID
@Entity
@Table(name = "invoice_items", indexes = [
    Index(name = "idx_invoice_item_product", columnList = "product_id"),
    Index(name = "idx_invoice_item_merchant_product", columnList = "merchant_product_id")
])
data class InvoiceItemEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "invoice_item_seq")
    @SequenceGenerator(name = "invoice_item_seq", sequenceName = "invoice_item_id_seq", allocationSize = 1)
    val id: Long = 0,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    val invoice: InvoiceEntity,
    
    @Column(name = "product_id", nullable = false)
    val productId: Long,
    
    @Column(name = "product_uuid", nullable = false)
    val productUuid: UUID,
    
    @Column(name = "merchant_product_id", nullable = false)
    val merchantProductId: Long,
    
    @Column(name = "product_name", nullable = false)
    val productName: String,
    
    @Column(name = "product_sku", nullable = false)
    val productSku: String,
    
    @Column(name = "description", length = 1000)
    val description: String? = null,
    
    @Column(name = "quantity", nullable = false, precision = 19, scale = 4)
    val quantity: BigDecimal,
    
    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    val unitPrice: BigDecimal,
    
    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 2)
    val taxRate: BigDecimal = BigDecimal.ZERO,
    
    @Column(name = "discount_percentage", nullable = false, precision = 5, scale = 2)
    val discountPercentage: BigDecimal = BigDecimal.ZERO,
    
    @Column(name = "line_total", nullable = false, precision = 19, scale = 4)
    val lineTotal: BigDecimal,
    
    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 4)
    val taxAmount: BigDecimal,
    
    @Column(name = "net_price", nullable = false, precision = 19, scale = 4)
    val netPrice: BigDecimal
) {
    fun toDomain(): InvoiceItem {
        return InvoiceItem(
            productId = productId,
            productUuid = productUuid,
            merchantProductId = merchantProductId,
            productName = productName,
            productSku = productSku,
            description = description,
            quantity = quantity,
            unitPrice = unitPrice,
            taxRate = taxRate,
            discountPercentage = discountPercentage
        )
    }
    
    companion object {
        fun fromDomain(item: InvoiceItem, invoice: InvoiceEntity): InvoiceItemEntity {
            return InvoiceItemEntity(
                invoice = invoice,
                productId = item.productId,
                productUuid = item.productUuid,
                merchantProductId = item.merchantProductId,
                productName = item.productName,
                productSku = item.productSku,
                description = item.description,
                quantity = item.quantity,
                unitPrice = item.unitPrice,
                taxRate = item.taxRate,
                discountPercentage = item.discountPercentage,
                lineTotal = item.lineTotal,
                taxAmount = item.taxAmount,
                netPrice = item.netPrice
            )
        }
    }
}