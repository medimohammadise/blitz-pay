@file:ApplicationModule(displayName = "outbound")

package com.elegant.software.quickpay.payments.qrpay

import com.elegant.software.quickpay.payments.truelayer.api.PaymentResult
import com.elegant.software.quickpay.payments.truelayer.outbound.PaymentService
import mu.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.modulith.ApplicationModule
import org.springframework.stereotype.Component

@Component
class PaymentResultListener(
    private val gateway: PaymentService,
) {
    companion object {
        private val LOG = KotlinLogging.logger {}
    }

    @EventListener
    fun on(e: PaymentResult) {
        LOG.info("Pyment result received {}", e)
    }
}