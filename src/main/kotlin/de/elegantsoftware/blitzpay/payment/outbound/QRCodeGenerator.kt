package de.elegantsoftware.blitzpay.payment.outbound

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream
@Component
class QRCodeGenerator {
    
    private val logger = LoggerFactory.getLogger(QRCodeGenerator::class.java)
    
    fun generateQRCode(
        content: String,
        width: Int = 300,
        height: Int = 300,
        format: String = "PNG"
    ): ByteArray {
        return try {
            ByteArrayOutputStream().use { outputStream ->
                val qrCodeWriter = QRCodeWriter()
                val bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height)
                
                MatrixToImageWriter.writeToStream(bitMatrix, format, outputStream)
                outputStream.toByteArray()
            }
        } catch (e: Exception) {
            logger.error("Failed to generate QR code for content: $content", e)
            throw RuntimeException("Failed to generate QR code", e)
        }
    }
    
    fun generateQRCodeBase64(
        content: String,
        width: Int = 300,
        height: Int = 300
    ): String {
        val qrCodeBytes = generateQRCode(content, width, height)
        return java.util.Base64.getEncoder().encodeToString(qrCodeBytes)
    }
    
    fun generatePaymentQRCode(
        paymentUrl: String,
        amount: Double? = null,
        currency: String? = null,
        description: String? = null
    ): ByteArray {
        // Create enhanced QR code content with payment information
        val qrContent = buildString {
            append(paymentUrl)
            if (amount != null) {
                append("\nAmount: $amount")
            }
            if (currency != null) {
                append(" $currency")
            }
            if (description != null) {
                append("\nDescription: $description")
            }
        }
        
        return generateQRCode(qrContent, 350, 350)
    }
    
    fun generateQRCodeForPaymentLink(
        paymentUrl: String,
        merchantName: String? = null,
        amount: Double? = null,
        currency: String = "USD"
    ): Map<String, Any> {
        val qrCodeBytes = generatePaymentQRCode(paymentUrl, amount, currency, merchantName)
        val base64QrCode = java.util.Base64.getEncoder().encodeToString(qrCodeBytes)
        
        return mapOf(
            "qrCodeBytes" to qrCodeBytes,
            "qrCodeBase64" to base64QrCode,
            "paymentUrl" to paymentUrl,
            "metadata" to mapOf(
                "merchantName" to merchantName,
                "amount" to amount,
                "currency" to currency
            )
        )
    }
}