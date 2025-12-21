package de.elegantsoftware.blitzpay.invoice.outbound.persistence

import de.elegantsoftware.blitzpay.invoice.domain.*
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
@Entity
@Table(name = "invoices", indexes = [
    Index(name = "idx_invoice_merchant", columnList = "merchant_id"),
    Index(name = "idx_invoice_customer", columnList = "customer_id"),
    Index(name = "idx_invoice_status", columnList = "status"),
    Index(name = "idx_invoice_number", columnList = "invoice_number"),
    Index(name = "idx_invoice_uuid", columnList = "uuid", unique = true)
])
@SequenceGenerator(name = "invoice_seq", sequenceName = "invoice_id_seq", allocationSize = 1)
data class InvoiceEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "invoice_seq")
    val id: Long = 0,

    @Column(name = "uuid", nullable = false, unique = true)
    val uuid: UUID = UUID.randomUUID(),

    @Column(name = "merchant_id", nullable = false)
    val merchantId: Long,

    @Column(name = "merchant_uuid", nullable = false)
    val merchantUuid: UUID,

    @Column(name = "merchant_name", nullable = false)
    val merchantName: String,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "street", column = Column(name = "merchant_street")),
        AttributeOverride(name = "city", column = Column(name = "merchant_city")),
        AttributeOverride(name = "state", column = Column(name = "merchant_state")),
        AttributeOverride(name = "postalCode", column = Column(name = "merchant_postal_code")),
        AttributeOverride(name = "country", column = Column(name = "merchant_country"))
    )
    val merchantAddress: AddressEmbeddable,

    @Column(name = "customer_id")
    val customerId: Long?,

    @Column(name = "customer_uuid")
    val customerUuid: UUID?,

    @Column(name = "customer_name", nullable = false)
    val customerName: String,

    @Column(name = "customer_email")
    val customerEmail: String?,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "street", column = Column(name = "customer_street")),
        AttributeOverride(name = "city", column = Column(name = "customer_city")),
        AttributeOverride(name = "state", column = Column(name = "customer_state")),
        AttributeOverride(name = "postalCode", column = Column(name = "customer_postal_code")),
        AttributeOverride(name = "country", column = Column(name = "customer_country"))
    )
    val customerAddress: AddressEmbeddable?,

    @Column(name = "invoice_number", nullable = false)
    val invoiceNumber: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_type", nullable = false)
    val invoiceType: InvoiceType = InvoiceType.STANDARD,

    @Column(name = "issue_date", nullable = false)
    val issueDate: LocalDate,

    @Column(name = "due_date", nullable = false)
    val dueDate: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_term", nullable = false)
    val paymentTerm: PaymentTerm,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: InvoiceStatus = InvoiceStatus.DRAFT,

    @OneToMany(mappedBy = "invoice", cascade = [CascadeType.ALL], orphanRemoval = true)
    val items: MutableList<InvoiceItemEntity> = mutableListOf(),

    @Column(name = "subtotal", nullable = false, precision = 19, scale = 4)
    var subtotal: BigDecimal = BigDecimal.ZERO,

    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 4)
    var taxAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    var totalAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "currency", nullable = false, length = 3)
    val currency: String = "EUR",

    @Column(name = "notes", length = 2000)
    var notes: String? = null,

    @Column(name = "terms_and_conditions", length = 4000)
    var termsAndConditions: String? = null,

    @ElementCollection
    @CollectionTable(
        name = "invoice_payment_methods",
        joinColumns = [JoinColumn(name = "invoice_id")]
    )
    val paymentMethods: MutableList<PaymentMethodEmbeddable> = mutableListOf(),

    @Embedded
    val metadata: InvoiceMetadataEmbeddable = InvoiceMetadataEmbeddable(),

    @Version
    @Column(name = "version", nullable = false)
    val version: Long = 0
) {
    fun toDomain(): Invoice {
        return Invoice(
            id = InvoiceId(id),
            uuid = uuid,
            merchantId = merchantId,
            merchantUuid = merchantUuid,
            merchantName = merchantName,
            merchantAddress = merchantAddress.toDomain(),
            merchantTaxId = null,
            customerId = customerId,
            customerUuid = customerUuid,
            customerName = customerName,
            customerEmail = customerEmail,
            customerAddress = customerAddress?.toDomain(),
            customerTaxId = null,
            invoiceNumber = invoiceNumber,
            invoiceType = invoiceType,
            issueDate = issueDate,
            dueDate = dueDate,
            paymentTerm = paymentTerm,
            status = status,
            items = items.map { it.toDomain() }.toMutableList(),
            subtotal = subtotal,
            taxAmount = taxAmount,
            totalAmount = totalAmount,
            currency = currency,
            notes = notes,
            termsAndConditions = termsAndConditions,
            paymentMethods = paymentMethods.map { it.toDomain() },
            metadata = metadata.toDomain(),
            version = version
        )
    }

    companion object {
        fun fromDomain(invoice: Invoice): InvoiceEntity {
            val entity = InvoiceEntity(
                id = invoice.id.value,
                uuid = invoice.uuid,
                merchantId = invoice.merchantId,
                merchantUuid = invoice.merchantUuid,
                merchantName = invoice.merchantName,
                merchantAddress = AddressEmbeddable.fromDomain(invoice.merchantAddress),
                customerId = invoice.customerId,
                customerUuid = invoice.customerUuid,
                customerName = invoice.customerName,
                customerEmail = invoice.customerEmail,
                customerAddress = invoice.customerAddress?.let { AddressEmbeddable.fromDomain(it) },
                invoiceNumber = invoice.invoiceNumber,
                invoiceType = invoice.invoiceType,
                issueDate = invoice.issueDate,
                dueDate = invoice.dueDate,
                paymentTerm = invoice.paymentTerm,
                status = invoice.status,
                subtotal = invoice.subtotal,
                taxAmount = invoice.taxAmount,
                totalAmount = invoice.totalAmount,
                currency = invoice.currency,
                notes = invoice.notes,
                termsAndConditions = invoice.termsAndConditions,
                metadata = InvoiceMetadataEmbeddable.fromDomain(invoice.metadata)
            )

            // Add payment methods
            invoice.paymentMethods.forEach { method ->
                entity.paymentMethods.add(PaymentMethodEmbeddable.fromDomain(method))
            }

            // Add items
            invoice.items.forEach { item ->
                entity.items.add(InvoiceItemEntity.fromDomain(item, entity))
            }

            return entity
        }
    }
}

@Embeddable
data class AddressEmbeddable(
    @Column(name = "street", length = 500)
    val street: String,

    @Column(name = "city", length = 100)
    val city: String,

    @Column(name = "state", length = 100)
    val state: String? = null,

    @Column(name = "postal_code", length = 20)
    val postalCode: String,

    @Column(name = "country", length = 100)
    val country: String,

    @Column(name = "phone", length = 50)
    val phone: String? = null,

    @Column(name = "email", length = 255)
    val email: String? = null
) {
    fun toDomain(): Address {
        return Address(
            street = street,
            city = city,
            state = state,
            postalCode = postalCode,
            country = country,
            phone = phone,
            email = email
        )
    }

    companion object {
        fun fromDomain(address: Address): AddressEmbeddable {
            return AddressEmbeddable(
                street = address.street,
                city = address.city,
                state = address.state,
                postalCode = address.postalCode,
                country = address.country,
                phone = address.phone,
                email = address.email
            )
        }
    }
}

@Embeddable
data class PaymentMethodEmbeddable(
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    val type: PaymentMethodType,

    @Column(name = "details", length = 1000)
    val details: String? = null,

    @Column(name = "is_enabled", nullable = false)
    val isEnabled: Boolean = true
) {
    fun toDomain(): PaymentMethod {
        return PaymentMethod(
            type = type,
            details = details,
            isEnabled = isEnabled
        )
    }

    companion object {
        fun fromDomain(method: PaymentMethod): PaymentMethodEmbeddable {
            return PaymentMethodEmbeddable(
                type = method.type,
                details = method.details,
                isEnabled = method.isEnabled
            )
        }
    }
}

@Embeddable
data class InvoiceMetadataEmbeddable(
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "sent_date")
    var sentDate: LocalDate? = null,

    @Column(name = "paid_date")
    var paidDate: LocalDate? = null,

    @Column(name = "overdue_date")
    var overdueDate: LocalDate? = null,

    @Column(name = "reminder_sent_date")
    var reminderSentDate: LocalDate? = null,

    @Column(name = "qr_code_generated")
    var qrCodeGenerated: Boolean = false,

    @Column(name = "qr_code_generated_at")
    var qrCodeGeneratedAt: LocalDateTime? = null,

    @ElementCollection
    @CollectionTable(name = "invoice_custom_fields", joinColumns = [JoinColumn(name = "invoice_id")])
    @MapKeyColumn(name = "field_key")
    @Column(name = "field_value")
    val customFields: MutableMap<String, String> = mutableMapOf()
) {
    fun toDomain(): InvoiceMetadata {
        return InvoiceMetadata(
            createdAt = createdAt,
            updatedAt = updatedAt,
            sentDate = sentDate,
            paidDate = paidDate,
            overdueDate = overdueDate,
            reminderSentDate = reminderSentDate,
            qrCodeGenerated = qrCodeGenerated,
            qrCodeGeneratedAt = qrCodeGeneratedAt,
            customFields = customFields
        )
    }

    companion object {
        fun fromDomain(metadata: InvoiceMetadata): InvoiceMetadataEmbeddable {
            return InvoiceMetadataEmbeddable(
                createdAt = metadata.createdAt,
                updatedAt = metadata.updatedAt,
                sentDate = metadata.sentDate,
                paidDate = metadata.paidDate,
                overdueDate = metadata.overdueDate,
                reminderSentDate = metadata.reminderSentDate,
                qrCodeGenerated = metadata.qrCodeGenerated,
                qrCodeGeneratedAt = metadata.qrCodeGeneratedAt,
                customFields = metadata.customFields
            )
        }
    }
}