package com.elegant.software.quickpay.payments.truelayer.inbound

import com.elegant.software.quickpay.payments.truelayer.support.TlSignatureVerifier
import com.elegant.software.quickpay.payments.truelayer.domain.PaymentEvents
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

//@RestController
//@RequestMapping("/webhooks/truelayer")
class TrueLayerWebhookController(
    private val verifier: TlSignatureVerifier,
    private val publisher: ApplicationEventPublisher
) {

    @PostMapping
    fun handle(
        @RequestHeader("Tl-Signature") signature: String,
        @RequestBody rawBody: String
    ): ResponseEntity<Void> {
        verifier.verify(signature, rawBody)

        when {
            rawBody.contains("\"status\":\"settled\"") -> {
                val pid = extract(rawBody, "\"payment_id\":\"", "\"")
                publisher.publishEvent(PaymentEvents.PaymentSettled(pid))
            }
            rawBody.contains("\"status\":\"failed\"") -> {
                val pid = extract(rawBody, "\"payment_id\":\"", "\"")
                publisher.publishEvent(PaymentEvents.PaymentFailed(pid, "failed"))
            }
        }

        return ResponseEntity.ok().build()
    }

    private fun extract(source: String, start: String, end: String): String? {
        val i = source.indexOf(start)
        if (i < 0) return null
        val j = source.indexOf(end, i + start.length)
        if (j < 0) return null
        return source.substring(i + start.length, j)
    }
}
