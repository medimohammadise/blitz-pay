package de.elegantsoftware.blitzpay.sales.support.service

import de.elegantsoftware.blitzpay.merchant.domain.MerchantRepository
import de.elegantsoftware.blitzpay.sales.api.InvoiceServicePort
import de.elegantsoftware.blitzpay.sales.api.SalesEventPublisher
import de.elegantsoftware.blitzpay.sales.domain.*
import de.elegantsoftware.blitzpay.sales.inbound.web.dto.*
import de.elegantsoftware.blitzpay.sales.support.exception.*
import de.elegantsoftware.blitzpay.sales.support.mapper.InvoiceMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.*

@Service
@Transactional
class InvoiceService(
    private val invoiceRepository: InvoiceRepository,
    private val productRepository: ProductRepository,
    private val merchantRepository: MerchantRepository,
    private val invoiceMapper: InvoiceMapper,
    private val eventPublisher: SalesEventPublisher
) : InvoiceServicePort {

    override fun createInvoice(request: CreateInvoiceRequest): InvoiceResponse {
        // Fetch merchant by public ID
        val merchant = merchantRepository.findByPublicId(request.merchantId)
            .orElseThrow {
                de.elegantsoftware.blitzpay.merchant.support.exception.MerchantNotFoundException(
                    "Merchant with id ${request.merchantId} not found"
                )
            }

        val invoice = Invoice.create(
            merchantId = merchant.publicId,
            merchantName = request.merchantName ?: merchant.businessName,
            merchantEmail = request.merchantEmail ?: merchant.email,
            dueDate = request.dueDate ?: LocalDate.now().plusDays(30)
        ).apply {
            billingAddress = request.billingAddress?.let { address ->
                BillingAddress(
                    street = address.street,
                    city = address.city,
                    postalCode = address.postalCode,
                    country = address.country
                )
            }
            notes = request.notes
        }

        val savedInvoice = invoiceRepository.save(invoice)
        eventPublisher.publishInvoiceCreated(savedInvoice)

        return invoiceMapper.toResponse(savedInvoice)
    }

    override fun getInvoice(id: UUID): InvoiceResponse {
        val invoice = invoiceRepository.findByPublicId(id)
            ?: throw InvoiceNotFoundException("Invoice with id $id not found")

        return invoiceMapper.toResponse(invoice)
    }

    override fun getInvoiceByNumber(invoiceNumber: String): InvoiceResponse {
        val invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
            ?: throw
                InvoiceNotFoundException("Invoice with number $invoiceNumber not found")


        return invoiceMapper.toResponse(invoice)
    }

    override fun addInvoiceItem(invoiceId: UUID, request: AddInvoiceItemRequest): InvoiceResponse {
        val invoice = invoiceRepository.findByPublicId(invoiceId)
            ?: throw InvoiceNotFoundException("Invoice with id $invoiceId not found")

        val product = productRepository.findByPublicId(request.productId)
            .orElseThrow { ProductNotFoundException("Product with id ${request.productId} not found") }

        if (!product.isAvailable()) {
            throw InsufficientStockException("Product ${product.name} is not available")
        }

        val productInfo = InvoiceItemProductInfo(
            id = product.publicId,
            sku = product.sku,
            name = product.name,
            taxRate = product.taxInfo?.rate
        )

        val unitPrice = request.unitPrice?.let { Money(it, product.price.currency) }
            ?: product.price

        // Use the new addItem method with productInfo
        invoice.addItem(
            productInfo = productInfo,
            quantity = request.quantity,
            unitPrice = unitPrice
        )

        // Update product stock
        product.updateStock(-request.quantity)
        productRepository.save(product)

        val updatedInvoice = invoiceRepository.save(invoice)
        return invoiceMapper.toResponse(updatedInvoice)
    }

    override fun issueInvoice(id: UUID): InvoiceResponse {
        val invoice = invoiceRepository.findByPublicId(id)
            ?: throw InvoiceNotFoundException("Invoice with id $id not found")

        try {
            invoice.issue()
        } catch (ex: IllegalStateException) {
            throw InvoiceInvalidStatusException(ex.message)
        }

        val issuedInvoice = invoiceRepository.save(invoice)
        eventPublisher.publishInvoiceIssued(issuedInvoice)

        return invoiceMapper.toResponse(issuedInvoice)
    }

    override fun markInvoiceAsPaid(id: UUID, request: MarkInvoicePaidRequest): InvoiceResponse {
        val invoice = invoiceRepository.findByPublicId(id)
            ?: throw InvoiceNotFoundException("Invoice with id $id not found")

        invoice.markAsPaid(request.paymentDate ?: LocalDate.now())

        val paidInvoice = invoiceRepository.save(invoice)
        eventPublisher.publishInvoicePaid(paidInvoice)

        return invoiceMapper.toResponse(paidInvoice)
    }

    override fun cancelInvoice(id: UUID, reason: String?): InvoiceResponse {
        val invoice = invoiceRepository.findByPublicId(id)
            ?: throw InvoiceNotFoundException("Invoice with id $id not found")

        try {
            invoice.cancel(reason)
        } catch (ex: IllegalStateException) {
            throw InvoiceInvalidStatusException(ex.message)
        }

        val cancelledInvoice = invoiceRepository.save(invoice)
        return invoiceMapper.toResponse(cancelledInvoice)
    }

    override fun getMerchantInvoices(merchantId: UUID): List<InvoiceResponse> {
        // Use a query that filters by merchant's public ID stored in merchantInfo
        val invoices = invoiceRepository.findAllByMerchantId(merchantId)

        return invoices.map(invoiceMapper::toResponse)
    }
}