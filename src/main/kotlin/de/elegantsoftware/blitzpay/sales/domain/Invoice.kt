package de.elegantsoftware.blitzpay.sales.domain

import de.elegantsoftware.blitzpay.common.domain.BaseEntity
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "invoices")
class Invoice(
    @Column(nullable = false, unique = true)
    val invoiceNumber: String,

    // Use embeddable value object for merchant info (not entity)
    @Embedded
    var merchantInfo: InvoiceMerchantInfo,

    @OneToMany(
        mappedBy = "invoice",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    val items: MutableList<InvoiceItem> = mutableListOf(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: InvoiceStatus = InvoiceStatus.DRAFT,

    @Column(nullable = false)
    var issueDate: LocalDate = LocalDate.now(),

    @Column(nullable = false)
    var dueDate: LocalDate,

    @Embedded
    var billingAddress: BillingAddress? = null,

    @Column(length = 1000)
    var notes: String? = null,

    @Embedded
    var totals: InvoiceTotals = InvoiceTotals()

) : BaseEntity() {

    companion object {
        fun create(
            merchantId: UUID,
            merchantName: String,
            merchantEmail: String,
            dueDate: LocalDate = LocalDate.now().plusDays(30),
            invoiceNumber: String = generateInvoiceNumber()
        ): Invoice {
            require(dueDate.isAfter(LocalDate.now())) {
                "Due date must be in the future"
            }

            return Invoice(
                invoiceNumber = invoiceNumber,
                merchantInfo = InvoiceMerchantInfo(
                    id = merchantId,
                    name = merchantName,
                    email = merchantEmail
                ),
                dueDate = dueDate
            )
        }

        private fun generateInvoiceNumber(): String {
            return "INV-${LocalDate.now().year}-${UUID.randomUUID().toString().substring(0, 8).uppercase()}"
        }
    }

    fun addItem(productInfo: InvoiceItemProductInfo, quantity: Int, unitPrice: Money) {
        require(status == InvoiceStatus.DRAFT) {
            "Cannot add items to a $status invoice"
        }

        val item = InvoiceItem(
            productInfo = productInfo,
            quantity = quantity,
            unitPrice = unitPrice,
            invoice = this
        )

        items.add(item)
        calculateTotals()
    }

    fun removeItem(itemPublicId: UUID) {
        require(status == InvoiceStatus.DRAFT) {
            "Cannot remove items from a $status invoice"
        }

        items.removeIf { it.publicId == itemPublicId }
        calculateTotals()
    }

    fun issue() {
        require(status == InvoiceStatus.DRAFT) {
            "Cannot issue a $status invoice"
        }
        require(items.isNotEmpty()) { "Cannot issue empty invoice" }

        status = InvoiceStatus.ISSUED
        issueDate = LocalDate.now()
    }

    fun markAsPaid(paymentDate: LocalDate = LocalDate.now()) {
        require(status == InvoiceStatus.ISSUED) {
            "Only issued invoices can be marked as paid"
        }

        status = InvoiceStatus.PAID
        totals.paidAmount = totals.totalAmount
        totals.paymentDate = paymentDate
    }

    fun cancel(reason: String? = null) {
        require(status == InvoiceStatus.DRAFT || status == InvoiceStatus.ISSUED) {
            "Cannot cancel a $status invoice"
        }

        status = InvoiceStatus.CANCELLED
        this.notes = reason?.let { "$notes\nCancelled: $it" } ?: notes
    }

    private fun calculateTotals() {
        val subtotal = items.sumOf { it.lineTotal.amount }
        val tax = items.sumOf { it.taxAmount ?: BigDecimal.ZERO }

        totals = InvoiceTotals(
            subtotalAmount = Money(subtotal, items.firstOrNull()?.unitPrice?.currency ?: "EUR"),
            taxAmount = Money(tax, items.firstOrNull()?.unitPrice?.currency ?: "EUR"),
            totalAmount = Money(subtotal + tax, items.firstOrNull()?.unitPrice?.currency ?: "EUR")
        )
    }
}

// Embedded Value Objects
@Embeddable
data class InvoiceTotals(
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "subtotal_amount")),
        AttributeOverride(name = "currency", column = Column(name = "subtotal_currency"))
    )
    var subtotalAmount: Money = Money(BigDecimal.ZERO),

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "tax_amount")),
        AttributeOverride(name = "currency", column = Column(name = "tax_currency"))
    )
    var taxAmount: Money = Money(BigDecimal.ZERO),

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "total_amount")),
        AttributeOverride(name = "currency", column = Column(name = "total_currency"))
    )
    var totalAmount: Money = Money(BigDecimal.ZERO),

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "paid_amount")),
        AttributeOverride(name = "currency", column = Column(name = "paid_currency"))
    )
    var paidAmount: Money = Money(BigDecimal.ZERO),

    @Column(name = "payment_date")
    var paymentDate: LocalDate? = null
)

@Entity
@Table(name = "invoice_items")
class InvoiceItem(
    @Embedded
    val productInfo: InvoiceItemProductInfo,

    @Column(nullable = false)
    var quantity: Int,

    @Embedded
    var unitPrice: Money,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false, referencedColumnName = "id")
    var invoice: Invoice? = null

) : BaseEntity() {

    init {
        require(quantity > 0) { "Quantity must be positive" }
    }

    val lineTotal: Money
        get() = unitPrice * quantity

    val taxAmount: BigDecimal?
        get() = productInfo.taxRate?.let {
            lineTotal.amount.multiply(it)
        }
}

// Reference Objects (for loose coupling)
@Embeddable
data class InvoiceMerchantInfo(
    @Column(name = "merchant_id")
    val id: UUID,  // Store UUID (public ID)

    @Column(name = "merchant_name")
    val name: String,

    @Column(name = "merchant_email")
    val email: String
)

@Embeddable
data class InvoiceItemProductInfo(
    @Column(name = "product_id")
    val id: UUID,

    @Column(name = "product_sku")
    val sku: String,

    @Column(name = "product_name")
    val name: String,

    @Column(name = "product_tax_rate", precision = 5, scale = 3)
    val taxRate: BigDecimal? = null
)

@Embeddable
data class BillingAddress(
    @Column(name = "billing_street")
    val street: String,

    @Column(name = "billing_city")
    val city: String,

    @Column(name = "billing_postal_code")
    val postalCode: String,

    @Column(name = "billing_country", length = 2)
    val country: String
)

enum class InvoiceStatus {
    DRAFT, ISSUED, PAID, OVERDUE, CANCELLED
}