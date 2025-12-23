package de.elegantsoftware.blitzpay.payment.api

import de.elegantsoftware.blitzpay.gateways.api.PaymentGateway
import de.elegantsoftware.blitzpay.gateways.api.PaymentStatus
import de.elegantsoftware.blitzpay.gateways.api.GatewayType
import de.elegantsoftware.blitzpay.merchant.api.MerchantService
import de.elegantsoftware.blitzpay.payment.domain.Payment
import de.elegantsoftware.blitzpay.payment.domain.PaymentLink
import de.elegantsoftware.blitzpay.payment.outbound.PaymentLinkRepository
import de.elegantsoftware.blitzpay.payment.outbound.PaymentRepository
import de.elegantsoftware.blitzpay.payment.outbound.QRCodeGenerator
import de.elegantsoftware.blitzpay.product.api.ProductService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

@Service
@Transactional
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val paymentLinkRepository: PaymentLinkRepository,
    private val productService: ProductService,
    private val merchantService: MerchantService,
    private val paymentGateway: PaymentGateway,
    private val qrCodeGenerator: QRCodeGenerator
) {

    fun createPaymentLink(
        merchantId: UUID,
        productIds: List<UUID>,
        gatewayType: GatewayType = GatewayType.TRUELAYER,
        description: String = ""
    ): PaymentLink {
        // Validate merchant
        merchantService.validateMerchant(merchantId)

        // Get total amount
        val totalAmount = productService.getTotalPrice(productIds)

        // Get gateway provider
        val gatewayProvider = paymentGateway.getProvider(gatewayType)

        // Create payment with gateway
        val paymentResponse = gatewayProvider.createPayment(
            amount = totalAmount,
            currency = "USD",
            merchantId = merchantId.toString(),
            description = description
        )

        // Generate QR code if supported
        val qrCodeData = if (gatewayProvider.supportsQrCode()) {
            val qrCodeBytes = gatewayProvider.generateQrCode(paymentResponse.paymentId)
            qrCodeBytes?.let {
                Base64.getEncoder().encodeToString(it)
            }
        } else null

        // Create payment link
        val paymentLink = PaymentLink(
            merchantId = merchantId,
            productIds = productIds,
            amount = totalAmount,
            description = description,
            gatewayType = gatewayType,
            paymentUrl = paymentResponse.authorizationUrl ?: "",
            qrCodeUrl = paymentResponse.qrCodeUrl,
            qrCodeData = qrCodeData,
            expiresAt = Clock.System.now().plus(7.days)
        )

        return paymentLinkRepository.save(paymentLink)
    }

    fun createInvoice(
        merchantId: UUID,
        productIds: List<UUID>,
        customerEmail: String,
        description: String = ""
    ): Payment {
        // Validate merchant
        merchantService.validateMerchant(merchantId)

        // Get total amount
        val totalAmount = productService.getTotalPrice(productIds)

        // Create payment record (invoice)
        val payment = Payment(
            merchantId = merchantId,
            productIds = productIds,
            amount = totalAmount,
            description = description,
            customerEmail = customerEmail,
            status = PaymentStatus.PENDING
        )

        return paymentRepository.save(payment)
    }

    fun generateQRCodeForPayment(paymentId: UUID): ByteArray {
        val payment = paymentRepository.findById(paymentId)
            .orElseThrow { IllegalArgumentException("Payment not found: $paymentId") }

        val paymentLink = payment.paymentLinkId?.let {
            paymentLinkRepository.findById(it).orElse(null)
        }

        val url = paymentLink?.paymentUrl ?: payment.gatewayPaymentId?.let {
            // Generate payment URL from gateway payment ID
            "https://payment.example.com/pay/$it"
        } ?: throw IllegalStateException("No payment URL available")

        return qrCodeGenerator.generateQRCode(url, 300, 300)
    }

    fun processWebhook(
        gatewayType: GatewayType,
        paymentId: String,
        status: String
    ) {
        val payment = paymentRepository.findByGatewayPaymentId(paymentId)
            ?: throw IllegalArgumentException("Payment not found for gateway ID: $paymentId")

        val paymentStatus = when (status.lowercase()) {
            "success", "authorized", "completed" -> PaymentStatus.SUCCESS
            "failed", "rejected" -> PaymentStatus.FAILED
            "cancelled" -> PaymentStatus.CANCELLED
            else -> PaymentStatus.PENDING
        }

        payment.status = paymentStatus
        payment.updatedAt = Clock.System.now()

        if (paymentStatus == PaymentStatus.SUCCESS) {
            payment.paidAt = Clock.System.now()
        }

        paymentRepository.save(payment)
    }

    fun getPaymentById(paymentId: UUID): Payment {
        return paymentRepository.findById(paymentId)
            .orElseThrow { IllegalArgumentException("Payment not found: $paymentId") }
    }

    fun getPaymentsByMerchant(merchantId: UUID): List<Payment> {
        return paymentRepository.findByMerchantId(merchantId)
    }

    fun getPaymentLinkById(linkId: UUID): PaymentLink {
        return paymentLinkRepository.findById(linkId)
            .orElseThrow { IllegalArgumentException("Payment link not found: $linkId") }
    }

    fun getPaymentLinksByMerchant(merchantId: UUID): List<PaymentLink> {
        return paymentLinkRepository.findByMerchantIdAndIsActive(merchantId, true)
    }

    fun deactivatePaymentLink(linkId: UUID) {
        val paymentLink = paymentLinkRepository.findById(linkId)
            .orElseThrow { IllegalArgumentException("Payment link not found: $linkId") }

        paymentLinkRepository.save(paymentLink.copy(isActive = false))
    }
}