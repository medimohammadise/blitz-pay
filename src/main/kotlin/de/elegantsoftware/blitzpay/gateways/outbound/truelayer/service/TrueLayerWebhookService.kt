package de.elegantsoftware.blitzpay.gateways.outbound.truelayer.service

import com.fasterxml.jackson.databind.ObjectMapper
import de.elegantsoftware.blitzpay.gateways.outbound.truelayer.client.TrueLayerWebhookClient
import de.elegantsoftware.blitzpay.gateways.outbound.truelayer.model.TrueLayerWebhookPayload
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TrueLayerWebhookService(
    private val webhookClient: TrueLayerWebhookClient,
    private val signatureService: TrueLayerSignatureService,
    private val objectMapper: ObjectMapper
) {
    
    private val logger = LoggerFactory.getLogger(TrueLayerWebhookService::class.java)
    
    fun registerWebhook(baseUrl: String, path: String = "/api/webhooks/truelayer"): String {
        val webhookUrl = "$baseUrl$path"
        val response = webhookClient.registerWebhook(webhookUrl)
        
        return response["id"] as String? ?: throw IllegalStateException("No webhook ID returned")
    }
    
    fun verifyAndProcessWebhook(signatureHeader: String, rawBody: String): TrueLayerWebhookPayload {
        logger.info("Processing TrueLayer webhook with signature")
        
        // Verify signature
        if (!signatureService.verifyWebhookSignature(signatureHeader, rawBody)) {
            throw SecurityException("Invalid webhook signature")
        }
        
        // Parse payload
        return try {
            objectMapper.readValue(rawBody, TrueLayerWebhookPayload::class.java)
        } catch (e: Exception) {
            logger.error("Failed to parse webhook payload", e)
            throw IllegalArgumentException("Invalid webhook payload")
        }
    }
    
    fun getRegisteredWebhooks(): List<Map<String, Any>> {
        return webhookClient.listWebhooks()
    }
    
    fun deleteWebhook(webhookId: String) {
        webhookClient.deleteWebhook(webhookId)
    }
}