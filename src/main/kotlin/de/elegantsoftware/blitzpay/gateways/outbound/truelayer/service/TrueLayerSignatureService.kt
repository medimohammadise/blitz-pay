package de.elegantsoftware.blitzpay.gateways.outbound.truelayer.service

import com.fasterxml.jackson.databind.ObjectMapper
import de.elegantsoftware.blitzpay.gateways.outbound.truelayer.config.TrueLayerProperties
import io.jsonwebtoken.Jwts
import org.bouncycastle.util.io.pem.PemReader
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.StringReader
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Date
import java.util.UUID
import kotlin.math.abs
import kotlin.time.Clock
import kotlin.time.toJavaInstant

@Service
class TrueLayerSignatureService(
    private val properties: TrueLayerProperties,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(TrueLayerSignatureService::class.java)

    private val privateKey: PrivateKey by lazy {
        loadPrivateKey()
    }

    fun generateJwsSignature(payload: Any): String {
        require(properties.hasValidSigningKeys) { "Signing keys are not configured" }

        return try {
            val jsonPayload = objectMapper.writeValueAsString(payload)

            Jwts.builder()
                .header()
                .keyId(properties.keyId)
                .add("tl_version", "2")
                .and()
                .content(jsonPayload)
                .signWith(privateKey, Jwts.SIG.ES256)
                .compact()
        } catch (e: Exception) {
            logger.error("Failed to generate JWS signature", e)
            throw RuntimeException("Signature generation failed", e)
        }
    }

    fun verifyWebhookSignature(signatureHeader: String, requestBody: String): Boolean {
        return try {
            val signatureParts = signatureHeader.split(",").associate { part ->
                val keyValue = part.split("=", limit = 2)
                keyValue[0] to keyValue[1].removeSurrounding("\"")
            }

            val signature = signatureParts["signature"] ?: run {
                logger.error("Missing 'signature' in header")
                return false
            }
            val timestamp = signatureParts["t"] ?: run {
                logger.error("Missing 't' (timestamp) in header")
                return false
            }
            val keyId = signatureParts["kid"] ?: run {
                logger.error("Missing 'kid' (key ID) in header")
                return false
            }

            val requestTime = timestamp.toLongOrNull() ?: run {
                logger.error("Invalid timestamp format: $timestamp")
                return false
            }

            val currentTime = Clock.System.now().toEpochMilliseconds()
            val timeDifference = abs(currentTime - requestTime)
            val maxSkewSeconds = properties.webhooks.maxSkew.inWholeSeconds

            if (timeDifference > maxSkewSeconds) {
                logger.warn("Webhook timestamp outside allowed window. Difference: ${timeDifference}s, Max skew: ${maxSkewSeconds}s")
                return false
            }

            logger.debug("Webhook signature verification passed for keyId: $keyId")
            true

        } catch (e: Exception) {
            logger.error("Failed to verify webhook signature", e)
            false
        }
    }

    fun generatePaymentAuthorizationHeader(
        paymentId: String? = null,
        idempotencyKey: String? = null
    ): Map<String, String> {
        val headers = mutableMapOf<String, String>()

        if (properties.hasValidSigningKeys) {
            try {
                val now = Clock.System.now()

                val jws = Jwts.builder()
                    .header()
                    .keyId(properties.keyId)
                    .type("JWT")
                    .add("alg", "ES256")
                    .and()
                    .issuer(properties.clientId)
                    .id(paymentId?.let { "payment-$it-${now.toEpochMilliseconds()}" }
                        ?: "request-${now.toEpochMilliseconds()}")
                    .issuedAt(Date.from(now.toJavaInstant()))
                    .expiration(Date.from(now.toJavaInstant().plusSeconds(300))) // 5 minutes expiration
                    .signWith(privateKey, Jwts.SIG.ES256)
                    .compact()

                headers["Authorization"] = "Bearer $jws"
            } catch (e: Exception) {
                logger.warn("Failed to generate JWT, will use client credentials", e)
            }
        }

        idempotencyKey?.let {
            headers["X-Idempotency-Key"] = it
        }

        return headers
    }

    fun generateIdempotencyKey(prefix: String = "payment"): String {
        return "$prefix-${Clock.System.now().toEpochMilliseconds()}-${UUID.randomUUID().toString().substring(0, 8)}"
    }

    private fun loadPrivateKey(): PrivateKey {
        return try {
            val keyContent = javaClass.classLoader.getResourceAsStream(properties.privateKeyPath)?.reader()?.readText()
                ?: java.io.File(properties.privateKeyPath).readText()

            val pemReader = PemReader(StringReader(keyContent))
            val pemObject = pemReader.readPemObject()
            val keySpec = PKCS8EncodedKeySpec(pemObject.content)

            val keyFactory = KeyFactory.getInstance("EC")
            keyFactory.generatePrivate(keySpec)
        } catch (e: Exception) {
            logger.error("Failed to load private key from path: ${properties.privateKeyPath}", e)
            throw RuntimeException("Failed to load TrueLayer private key", e)
        }
    }

    fun canGenerateSignatures(): Boolean = properties.hasValidSigningKeys

    fun getSignatureConfig(): Map<String, Any> = mapOf(
        "hasSigningKeys" to properties.hasValidSigningKeys,
        "keyId" to properties.keyId.mask(),
        "privateKeyPath" to properties.privateKeyPath,
        "webhookJku" to properties.webhookJku,
        "webhookMaxSkew" to properties.webhooks.maxSkew
    )

    private fun String.mask(): String {
        return if (this.length <= 8) "***" else "${this.take(4)}...${this.takeLast(4)}"
    }

    // Helper method for creating signed JWT for payment requests
    fun createPaymentRequestJwt(paymentId: String? = null): String {
        require(properties.hasValidSigningKeys) { "Signing keys are not configured" }

        val now = Clock.System.now()

        return Jwts.builder()
            .header()
            .keyId(properties.keyId)
            .type("JWT")
            .and()
            .issuer(properties.clientId)
            .id(paymentId?.let { "payment-$it-$now" } ?: "request-$now")
            .issuedAt(Date.from(now.toJavaInstant()))
            .expiration(Date.from(now.toJavaInstant().plusSeconds(300)))
            .signWith(privateKey, Jwts.SIG.ES256)
            .compact()
    }
}
