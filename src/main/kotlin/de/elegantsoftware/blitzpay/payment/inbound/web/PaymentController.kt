package de.elegantsoftware.blitzpay.payment.inbound.web

import de.elegantsoftware.blitzpay.payment.api.PaymentService
import de.elegantsoftware.blitzpay.payment.domain.Payment
import de.elegantsoftware.blitzpay.payment.domain.PaymentLink
import de.elegantsoftware.blitzpay.payment.inbound.web.dto.*
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/payments")
class PaymentController(
    private val paymentService: PaymentService
) {

    private val logger = LoggerFactory.getLogger(PaymentController::class.java)

    @PostMapping("/links")
    fun createPaymentLink(@RequestBody request: CreatePaymentLinkRequest): ResponseEntity<PaymentLinkDto> {
        logger.info("Creating payment link for merchant: {}, products: {}", request.merchantId, request.productIds)

        val paymentLink = paymentService.createPaymentLink(
            merchantId = request.merchantId,
            productIds = request.productIds,
            gatewayType = request.gatewayType,
            description = request.description
        )

        return ResponseEntity.ok(toPaymentLinkDto(paymentLink))
    }

    @PostMapping("/invoices")
    fun createInvoice(@RequestBody request: CreateInvoiceRequest): ResponseEntity<PaymentDto> {
        logger.info("Creating invoice for merchant: {}, customer: {}", request.merchantId, request.customerEmail)

        val invoice = paymentService.createInvoice(
            merchantId = request.merchantId,
            productIds = request.productIds,
            customerEmail = request.customerEmail,
            description = request.description
        )

        return ResponseEntity.ok(toPaymentDto(invoice))
    }

    @GetMapping("/{paymentId}/qr-code")
    fun getQRCode(@PathVariable paymentId: UUID): ResponseEntity<ByteArray> {
        logger.info("Generating QR code for payment: {}", paymentId)

        val qrCode = paymentService.generateQRCodeForPayment(paymentId)

        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"payment-qr-$paymentId.png\"")
            .body(qrCode)
    }

    @GetMapping("/{paymentId}/qr-code/base64")
    fun getQRCodeBase64(@PathVariable paymentId: UUID): ResponseEntity<QRCodeResponse> {
        logger.info("Generating QR code (base64) for payment: {}", paymentId)

        val qrCodeBytes = paymentService.generateQRCodeForPayment(paymentId)
        val base64QrCode = Base64.getEncoder().encodeToString(qrCodeBytes)
        val payment = paymentService.getPaymentById(paymentId)
        val paymentUrl = payment.paymentLinkId?.let {
            paymentService.getPaymentLinkById(it).paymentUrl
        } ?: "https://payment.example.com/pay/${payment.gatewayPaymentId}"

        val response = QRCodeResponse(
            paymentId = paymentId,
            qrCodeBase64 = base64QrCode,
            qrCodeUrl = null,
            paymentUrl = paymentUrl
        )

        return ResponseEntity.ok(response)
    }

    @GetMapping("/{paymentId}")
    fun getPayment(@PathVariable paymentId: UUID): ResponseEntity<PaymentDto> {
        logger.debug("Getting payment: {}", paymentId)

        val payment = paymentService.getPaymentById(paymentId)

        return ResponseEntity.ok(toPaymentDto(payment))
    }

    @GetMapping("/merchant/{merchantId}")
    fun getMerchantPayments(@PathVariable merchantId: UUID): ResponseEntity<List<PaymentDto>> {
        logger.debug("Getting payments for merchant: {}", merchantId)

        val payments = paymentService.getPaymentsByMerchant(merchantId)

        return ResponseEntity.ok(payments.map { toPaymentDto(it) })
    }

    @GetMapping("/links/merchant/{merchantId}")
    fun getMerchantPaymentLinks(@PathVariable merchantId: UUID): ResponseEntity<List<PaymentLinkDto>> {
        logger.debug("Getting payment links for merchant: {}", merchantId)

        val paymentLinks = paymentService.getPaymentLinksByMerchant(merchantId)

        return ResponseEntity.ok(paymentLinks.map { toPaymentLinkDto(it) })
    }

    @GetMapping("/links/{linkId}")
    fun getPaymentLink(@PathVariable linkId: UUID): ResponseEntity<PaymentLinkDto> {
        logger.debug("Getting payment link: {}", linkId)

        val paymentLink = paymentService.getPaymentLinkById(linkId)

        return ResponseEntity.ok(toPaymentLinkDto(paymentLink))
    }

    @PatchMapping("/links/{linkId}/deactivate")
    fun deactivatePaymentLink(@PathVariable linkId: UUID): ResponseEntity<Void> {
        logger.info("Deactivating payment link: {}", linkId)

        paymentService.deactivatePaymentLink(linkId)

        return ResponseEntity.noContent().build()
    }

    // Mapper functions
    private fun toPaymentDto(payment: Payment): PaymentDto {
        return PaymentDto(
            id = payment.id,
            merchantId = payment.merchantId,
            productIds = payment.productIds,
            amount = payment.amount,
            currency = payment.currency,
            gatewayType = payment.gatewayType,
            gatewayPaymentId = payment.gatewayPaymentId,
            status = payment.status,
            customerEmail = payment.customerEmail,
            description = payment.description,
            createdAt = payment.createdAt,
            paidAt = payment.paidAt,
            paymentLinkId = payment.paymentLinkId
        )
    }

    private fun toPaymentLinkDto(paymentLink: PaymentLink): PaymentLinkDto {
        return PaymentLinkDto(
            id = paymentLink.id,
            merchantId = paymentLink.merchantId,
            productIds = paymentLink.productIds,
            amount = paymentLink.amount,
            currency = paymentLink.currency,
            description = paymentLink.description,
            gatewayType = paymentLink.gatewayType,
            paymentUrl = paymentLink.paymentUrl,
            qrCodeUrl = paymentLink.qrCodeUrl,
            expiresAt = paymentLink.expiresAt,
            isActive = paymentLink.isActive,
            createdAt = paymentLink.createdAt
        )
    }
}