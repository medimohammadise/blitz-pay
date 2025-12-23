package de.elegantsoftware.blitzpay.invoice.outbound

import de.elegantsoftware.blitzpay.invoice.api.*
import de.elegantsoftware.blitzpay.invoice.domain.*
import de.elegantsoftware.blitzpay.invoice.domain.events.*
import de.elegantsoftware.blitzpay.invoice.outbound.persistence.InvoiceEntity
import de.elegantsoftware.blitzpay.invoice.outbound.persistence.InvoiceRepository
import de.elegantsoftware.blitzpay.invoice.support.InvoiceNumberGenerator
import de.elegantsoftware.blitzpay.invoice.support.mapper.InvoiceMapper
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

@Service
class InvoiceServiceImpl(
    private val invoiceRepository: InvoiceRepository,
    private val invoiceNumberGenerator: InvoiceNumberGenerator,
    private val eventPublisher: ApplicationEventPublisher,
    private val invoiceMapper: InvoiceMapper
) : InvoiceService {

    override fun createInvoice(request: CreateInvoiceApiRequest): Invoice {
        val invoiceNumber = generateInvoiceNumber(request.merchantId)
        val dueDate = request.dueDate ?: calculateDueDate(request.issueDate, request.paymentTerm)

        // Convert API DTOs to domain models
        val merchantAddress = invoiceMapper.apiAddressToDomain(request.merchantAddress)
        val customerAddress = request.customerAddress?.let { invoiceMapper.apiAddressToDomain(it) }
        val paymentMethods = request.paymentMethods.map { invoiceMapper.apiPaymentMethodToDomain(it) }

        val invoice = Invoice(
            id = InvoiceId(0),
            uuid = UUID.randomUUID(),
            merchantId = request.merchantId,
            merchantUuid = request.merchantUuid,
            merchantName = request.merchantName,
            merchantTaxId = null, // Not provided in API request
            merchantAddress = merchantAddress,
            customerId = request.customerId,
            customerUuid = request.customerUuid,
            customerName = request.customerName,
            customerEmail = request.customerEmail,
            customerAddress = customerAddress,
            customerTaxId = null, // Not provided in API request
            invoiceNumber = invoiceNumber,
            invoiceType = request.invoiceType,
            issueDate = request.issueDate,
            dueDate = dueDate,
            paymentTerm = request.paymentTerm,
            status = InvoiceStatus.DRAFT,
            subtotal = BigDecimal.ZERO,
            taxAmount = BigDecimal.ZERO,
            totalAmount = BigDecimal.ZERO,
            currency = request.currency,
            notes = request.notes,
            termsAndConditions = request.termsAndConditions,
            paymentMethods = paymentMethods
            // items, metadata, and version are set by default values
        )

        // Add items
        request.items.forEach { itemRequest ->
            val item = InvoiceItem(
                productId = itemRequest.productId,
                productUuid = itemRequest.productUuid,
                merchantProductId = itemRequest.merchantProductId,
                productName = itemRequest.productName,
                productSku = itemRequest.productSku,
                description = itemRequest.description,
                quantity = itemRequest.quantity,
                unitPrice = itemRequest.unitPrice,
                taxRate = itemRequest.taxRate,
                discountPercentage = itemRequest.discountPercentage
            )
            invoice.addItem(item)
        }

        val entity = invoiceRepository.save(InvoiceEntity.fromDomain(invoice))
        val savedInvoice = entity.toDomain()

        eventPublisher.publishEvent(
            InvoiceCreatedEvent(
                invoiceId = savedInvoice.uuid,
                invoiceNumber = savedInvoice.invoiceNumber,
                merchantId = savedInvoice.merchantId,
                customerId = savedInvoice.customerId,
                totalAmount = savedInvoice.totalAmount.toString(),
                currency = savedInvoice.currency,
                status = savedInvoice.status.name
            )
        )

        return savedInvoice
    }
    
    override fun updateInvoice(request: UpdateInvoiceApiRequest): Invoice {
        val entity = invoiceRepository.findById(request.invoiceId)
            .orElseThrow { InvoiceNotFoundException("Invoice with id ${request.invoiceId} not found") }
        
        val invoice = entity.toDomain()
        
        request.notes?.let { invoice.notes = it }
        request.termsAndConditions?.let { invoice.termsAndConditions = it }
        request.customerAddress?.let {
            invoice.customerAddress = invoiceMapper.apiAddressToDomain(it)
        }
        request.paymentMethods?.let {
            invoice.paymentMethods = it.map { pm -> invoiceMapper.apiPaymentMethodToDomain(pm) }
        }
        
        val updatedEntity = invoiceRepository.save(InvoiceEntity.fromDomain(invoice))
        return updatedEntity.toDomain()
    }
    
    override fun addItem(request: AddInvoiceItemApiRequest): Invoice {
        val entity = invoiceRepository.findById(request.invoiceId)
            .orElseThrow { InvoiceNotFoundException("Invoice with id ${request.invoiceId} not found") }
        
        val invoice = entity.toDomain()
        
        val item = InvoiceItem(
            productId = request.productId,
            productUuid = request.productUuid,
            merchantProductId = request.merchantProductId,
            productName = request.productName,
            productSku = request.productSku,
            description = request.description,
            quantity = request.quantity,
            unitPrice = request.unitPrice,
            taxRate = request.taxRate,
            discountPercentage = request.discountPercentage
        )
        
        invoice.addItem(item)
        
        val updatedEntity = invoiceRepository.save(InvoiceEntity.fromDomain(invoice))
        return updatedEntity.toDomain()
    }
    
    override fun removeItem(request: RemoveInvoiceItemApiRequest): Invoice {
        val entity = invoiceRepository.findById(request.invoiceId)
            .orElseThrow { InvoiceNotFoundException("Invoice with id ${request.invoiceId} not found") }
        
        val invoice = entity.toDomain()
        invoice.removeItem(request.productId)
        
        val updatedEntity = invoiceRepository.save(InvoiceEntity.fromDomain(invoice))
        return updatedEntity.toDomain()
    }
    
    override fun issueInvoice(invoiceId: Long): Invoice {
        val entity = invoiceRepository.findById(invoiceId)
            .orElseThrow { InvoiceNotFoundException("Invoice with id $invoiceId not found") }
        
        val invoice = entity.toDomain()
        invoice.issue()
        
        val updatedEntity = invoiceRepository.save(InvoiceEntity.fromDomain(invoice))
        val issuedInvoice = updatedEntity.toDomain()
        
        eventPublisher.publishEvent(
            InvoiceIssuedEvent(
                invoiceId = issuedInvoice.uuid,
                invoiceNumber = issuedInvoice.invoiceNumber,
                merchantId = issuedInvoice.merchantId,
                customerId = issuedInvoice.customerId,
                issueDate = issuedInvoice.issueDate,
                dueDate = issuedInvoice.dueDate,
                totalAmount = issuedInvoice.totalAmount.toString()
            )
        )
        
        return issuedInvoice
    }
    
    override fun sendInvoice(invoiceId: Long): Invoice {
        val entity = invoiceRepository.findById(invoiceId)
            .orElseThrow { InvoiceNotFoundException("Invoice with id $invoiceId not found") }
        
        val invoice = entity.toDomain()
        invoice.send()
        
        val updatedEntity = invoiceRepository.save(InvoiceEntity.fromDomain(invoice))
        return updatedEntity.toDomain()
    }
    
    override fun markAsPaid(invoiceId: Long, paymentDetails: PaymentDetailsApi): Invoice {
        val entity = invoiceRepository.findById(invoiceId)
            .orElseThrow { InvoiceNotFoundException("Invoice with id $invoiceId not found") }
        
        val invoice = entity.toDomain()
        invoice.markAsPaid(paymentDetails.paymentDate)
        
        val updatedEntity = invoiceRepository.save(InvoiceEntity.fromDomain(invoice))
        val paidInvoice = updatedEntity.toDomain()
        
        eventPublisher.publishEvent(
            InvoicePaidEvent(
                invoiceId = paidInvoice.uuid,
                invoiceNumber = paidInvoice.invoiceNumber,
                merchantId = paidInvoice.merchantId,
                customerId = paidInvoice.customerId,
                amountPaid = paymentDetails.amount.toString(),
                paymentMethod = paymentDetails.paymentMethod.name,
                paymentReference = paymentDetails.reference,
                paymentDate = paymentDetails.paymentDate
            )
        )
        
        return paidInvoice
    }
    
    override fun markAsPartiallyPaid(invoiceId: Long, amountPaid: BigDecimal): Invoice {
        val entity = invoiceRepository.findById(invoiceId)
            .orElseThrow { InvoiceNotFoundException("Invoice with id $invoiceId not found") }
        
        val invoice = entity.toDomain()
        invoice.markAsPartiallyPaid()
        
        val updatedEntity = invoiceRepository.save(InvoiceEntity.fromDomain(invoice))
        return updatedEntity.toDomain()
    }
    
    override fun cancelInvoice(invoiceId: Long, reason: String): Invoice {
        val entity = invoiceRepository.findById(invoiceId)
            .orElseThrow { InvoiceNotFoundException("Invoice with id $invoiceId not found") }
        
        val invoice = entity.toDomain()
        invoice.cancel(reason)
        
        val updatedEntity = invoiceRepository.save(InvoiceEntity.fromDomain(invoice))
        return updatedEntity.toDomain()
    }
    
    override fun generateQRCode(invoiceId: Long): String {
        val entity = invoiceRepository.findById(invoiceId)
            .orElseThrow { InvoiceNotFoundException("Invoice with id $invoiceId not found") }
        
        val invoice = entity.toDomain()
        
        entity.metadata.qrCodeGenerated = true
        entity.metadata.qrCodeGeneratedAt = Clock.System.now()
        invoiceRepository.save(entity)
        
        eventPublisher.publishEvent(
            InvoiceQRCodeGeneratedEvent(
                invoiceId = invoice.uuid,
                invoiceNumber = invoice.invoiceNumber,
                merchantId = invoice.merchantId,
                amount = invoice.totalAmount.toString(),
                currency = invoice.currency,
                qrCodeType = "PAYMENT_REQUEST"
            )
        )
        
        return "INVOICE:${invoice.uuid}:AMOUNT:${invoice.totalAmount}:CURRENCY:${invoice.currency}"
    }
    
    override fun getInvoiceById(id: Long): Invoice? {
        return invoiceRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)
    }
    
    override fun getInvoiceByUuid(uuid: UUID): Invoice? {
        return invoiceRepository.findByUuid(uuid)?.toDomain()
    }
    
    override fun getInvoicesByMerchant(merchantId: Long, page: Int, size: Int): List<Invoice> {
        val pageable = PageRequest.of(page, size)
        return invoiceRepository.findByMerchantId(merchantId, pageable)
            .map { it.toDomain() }
            .toList()
    }
    
    override fun getInvoicesByCustomer(customerId: Long, merchantId: Long): List<Invoice> {
        return invoiceRepository.findByMerchantIdAndCustomerId(merchantId, customerId)
            .map { it.toDomain() }
    }
    
    override fun findOverdueInvoices(merchantId: Long?): List<Invoice> {
        return if (merchantId != null) {
            invoiceRepository.findOverdueInvoicesByMerchant(merchantId, Clock.System.now())
                .map { it.toDomain() }
        } else {
            invoiceRepository.findAllOverdueInvoices(Clock.System.now())
                .map { it.toDomain() }
        }
    }
    
    override fun generateInvoiceNumber(merchantId: Long): String {
        return invoiceNumberGenerator.generate(merchantId)
    }
    
    override fun calculateTaxSummary(invoiceId: Long): TaxSummaryApi {
        val invoice = getInvoiceById(invoiceId)
            ?: throw InvoiceNotFoundException("Invoice with id $invoiceId not found")
        
        val taxRates = invoice.items
            .groupBy { it.taxRate }
            .mapValues { (_, items) -> items.sumOf { it.taxAmount } }
        
        return TaxSummaryApi(
            taxableAmount = invoice.subtotal,
            taxAmount = invoice.taxAmount,
            taxRates = taxRates
        )
    }
    
    override fun generateInvoicePdf(invoiceId: Long): ByteArray {
        val invoice = getInvoiceById(invoiceId)
            ?: throw InvoiceNotFoundException("Invoice with id $invoiceId not found")
        
        val pdfContent = """
            Invoice: ${invoice.invoiceNumber}
            Merchant: ${invoice.merchantName}
            Customer: ${invoice.customerName}
            Total: ${invoice.totalAmount} ${invoice.currency}
            Status: ${invoice.status}
        """.trimIndent()
        
        return pdfContent.toByteArray()
    }
    
    private fun calculateDueDate(issueDate: Instant, paymentTerm: PaymentTerm): Instant {
        return when (paymentTerm) {
            PaymentTerm.NET_7 -> issueDate.plus(7.days)
            PaymentTerm.NET_14 -> issueDate.plus(14.days)
            PaymentTerm.NET_30 -> issueDate.plus(30.days)
            PaymentTerm.NET_60 -> issueDate.plus(60.days)
            PaymentTerm.DUE_ON_RECEIPT -> issueDate
            PaymentTerm.CUSTOM -> issueDate.plus(30.days)
        }
    }
}

class InvoiceNotFoundException(message: String) : RuntimeException(message)