package com.elegant.software.quickpay.payments.qrpay

import com.elegant.software.quickpay.payments.truelayer.api.PaymentRequested
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/payments")
class PaymentRequestController(private val eventPublisher: ApplicationEventPublisher) {
    @PostMapping("/request")
    fun createPaymentRequest(@RequestBody request: PaymentRequested): ResponseEntity<String> {
        eventPublisher.publishEvent(request)
        return ResponseEntity.ok("PaymentRequested event registered.")
    }


}