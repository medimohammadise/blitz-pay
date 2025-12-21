package de.elegantsoftware.blitzpay.invoice.support.mapper

import de.elegantsoftware.blitzpay.invoice.api.*
import de.elegantsoftware.blitzpay.invoice.domain.*
import de.elegantsoftware.blitzpay.invoice.inbound.web.dto.*
import org.springframework.stereotype.Component

@Component
class InvoiceMapper {

    // === Web DTO → API Request (for controller to service) ===

    fun toApiRequest(webRequest: CreateInvoiceWebRequest): CreateInvoiceApiRequest {
        return CreateInvoiceApiRequest(
            merchantId = webRequest.merchantId,
            merchantUuid = webRequest.merchantUuid,
            merchantName = webRequest.merchantName,
            merchantAddress = webAddressToApi(webRequest.merchantAddress),
            customerId = webRequest.customerId,
            customerUuid = webRequest.customerUuid,
            customerName = webRequest.customerName,
            customerEmail = webRequest.customerEmail,
            customerAddress = webRequest.customerAddress?.let { webAddressToApi(it) },
            invoiceType = InvoiceType.valueOf(webRequest.invoiceType),
            issueDate = webRequest.issueDate,
            dueDate = webRequest.dueDate,
            paymentTerm = PaymentTerm.valueOf(webRequest.paymentTerm),
            currency = webRequest.currency,
            notes = webRequest.notes,
            termsAndConditions = webRequest.termsAndConditions,
            paymentMethods = webRequest.paymentMethods.map { webPaymentMethodToApi(it) },
            items = webRequest.items.map { webItemToApiRequest(it) }
        )
    }

    fun toApiRequest(webRequest: UpdateInvoiceWebRequest, invoiceId: Long): UpdateInvoiceApiRequest {
        return UpdateInvoiceApiRequest(
            invoiceId = invoiceId,
            notes = webRequest.notes,
            termsAndConditions = webRequest.termsAndConditions,
            customerAddress = webRequest.customerAddress?.let { webAddressToApi(it) },
            paymentMethods = webRequest.paymentMethods?.map { webPaymentMethodToApi(it) }
        )
    }

    fun toApiRequest(webRequest: AddInvoiceItemWebRequest, invoiceId: Long): AddInvoiceItemApiRequest {
        return AddInvoiceItemApiRequest(
            invoiceId = invoiceId,
            productId = webRequest.productId,
            productUuid = webRequest.productUuid,
            merchantProductId = webRequest.merchantProductId,
            productName = webRequest.productName,
            productSku = webRequest.productSku,
            description = webRequest.description,
            quantity = webRequest.quantity,
            unitPrice = webRequest.unitPrice,
            taxRate = webRequest.taxRate,
            discountPercentage = webRequest.discountPercentage
        )
    }

    fun toPaymentDetailsApi(webRequest: PaymentWebRequest): PaymentDetailsApi {
        return PaymentDetailsApi(
            amount = webRequest.amount,
            paymentDate = webRequest.paymentDate,
            paymentMethod = PaymentMethodType.valueOf(webRequest.paymentMethod),
            reference = webRequest.reference,
            transactionId = webRequest.transactionId
        )
    }

    // === Domain → Web Response (for service to controller) ===

    fun toWebResponse(invoice: Invoice): InvoiceWebResponse {
        return InvoiceWebResponse(
            id = invoice.id.value,
            uuid = invoice.uuid,
            merchantId = invoice.merchantId,
            merchantUuid = invoice.merchantUuid,
            merchantName = invoice.merchantName,
            merchantAddress = domainAddressToWeb(invoice.merchantAddress),
            customerId = invoice.customerId,
            customerUuid = invoice.customerUuid,
            customerName = invoice.customerName,
            customerEmail = invoice.customerEmail,
            customerAddress = invoice.customerAddress?.let { domainAddressToWeb(it) },
            invoiceNumber = invoice.invoiceNumber,
            invoiceType = invoice.invoiceType.name,
            issueDate = invoice.issueDate,
            dueDate = invoice.dueDate,
            paymentTerm = invoice.paymentTerm.name,
            status = invoice.status.name,
            items = invoice.items.map { domainItemToWeb(it) },
            subtotal = invoice.subtotal,
            taxAmount = invoice.taxAmount,
            totalAmount = invoice.totalAmount,
            currency = invoice.currency,
            notes = invoice.notes,
            termsAndConditions = invoice.termsAndConditions,
            paymentMethods = invoice.paymentMethods.map { domainPaymentMethodToWeb(it) },
            metadata = domainMetadataToWeb(invoice.metadata),
            version = invoice.version
        )
    }

    fun toWebTaxSummary(apiSummary: TaxSummaryApi): TaxSummaryWebResponse {
        return TaxSummaryWebResponse(
            taxableAmount = apiSummary.taxableAmount,
            taxAmount = apiSummary.taxAmount,
            taxRates = apiSummary.taxRates
        )
    }

    // === API Request → Domain (for service implementation) ===

    fun apiAddressToDomain(apiAddress: AddressApi): Address {
        return Address(
            street = apiAddress.street,
            city = apiAddress.city,
            state = apiAddress.state,
            postalCode = apiAddress.postalCode,
            country = apiAddress.country,
            phone = apiAddress.phone,
            email = apiAddress.email
        )
    }

    fun apiPaymentMethodToDomain(apiPaymentMethod: PaymentMethodApi): PaymentMethod {
        return PaymentMethod(
            type = apiPaymentMethod.type,
            details = apiPaymentMethod.details,
            isEnabled = apiPaymentMethod.isEnabled
        )
    }

    // === Helper: Web DTO → API DTO ===

    private fun webAddressToApi(webAddress: AddressWebRequest): AddressApi {
        return AddressApi(
            street = webAddress.street,
            city = webAddress.city,
            state = webAddress.state,
            postalCode = webAddress.postalCode,
            country = webAddress.country,
            phone = webAddress.phone,
            email = webAddress.email
        )
    }

    private fun webPaymentMethodToApi(webMethod: PaymentMethodWebRequest): PaymentMethodApi {
        return PaymentMethodApi(
            type = PaymentMethodType.valueOf(webMethod.type),
            details = webMethod.details,
            isEnabled = webMethod.isEnabled
        )
    }

    private fun webItemToApiRequest(webItem: CreateInvoiceItemWebRequest): CreateInvoiceItemApiRequest {
        return CreateInvoiceItemApiRequest(
            productId = webItem.productId,
            productUuid = webItem.productUuid,
            merchantProductId = webItem.merchantProductId,
            productName = webItem.productName,
            productSku = webItem.productSku,
            description = webItem.description,
            quantity = webItem.quantity,
            unitPrice = webItem.unitPrice,
            taxRate = webItem.taxRate,
            discountPercentage = webItem.discountPercentage
        )
    }

    // === Helper: Domain → Web DTO ===

    private fun domainAddressToWeb(address: Address): AddressWebResponse {
        return AddressWebResponse(
            street = address.street,
            city = address.city,
            state = address.state,
            postalCode = address.postalCode,
            country = address.country,
            phone = address.phone,
            email = address.email
        )
    }

    private fun domainPaymentMethodToWeb(method: PaymentMethod): PaymentMethodWebResponse {
        return PaymentMethodWebResponse(
            type = method.type.name,
            details = method.details,
            isEnabled = method.isEnabled
        )
    }

    private fun domainItemToWeb(item: InvoiceItem): InvoiceItemWebResponse {
        return InvoiceItemWebResponse(
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

    private fun domainMetadataToWeb(metadata: InvoiceMetadata): InvoiceMetadataWebResponse {
        return InvoiceMetadataWebResponse(
            createdAt = metadata.createdAt,
            updatedAt = metadata.updatedAt,
            sentDate = metadata.sentDate,
            paidDate = metadata.paidDate,
            overdueDate = metadata.overdueDate,
            reminderSentDate = metadata.reminderSentDate,
            qrCodeGenerated = metadata.qrCodeGenerated,
            qrCodeGeneratedAt = metadata.qrCodeGeneratedAt
        )
    }

    // === Helper: Domain → API DTO (if needed) ===

    fun domainAddressToApi(address: Address): AddressApi {
        return AddressApi(
            street = address.street,
            city = address.city,
            state = address.state,
            postalCode = address.postalCode,
            country = address.country,
            phone = address.phone,
            email = address.email
        )
    }

    fun domainPaymentMethodToApi(method: PaymentMethod): PaymentMethodApi {
        return PaymentMethodApi(
            type = method.type,
            details = method.details,
            isEnabled = method.isEnabled
        )
    }
}