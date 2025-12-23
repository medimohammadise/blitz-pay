package de.elegantsoftware.blitzpay.gateways.api

import de.elegantsoftware.blitzpay.gateways.support.GatewayFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GatewayConfiguration {
    
    @Bean
    fun paymentGateway(gatewayFactory: GatewayFactory): PaymentGateway {
        return PaymentGateway(gatewayFactory)
    }
}