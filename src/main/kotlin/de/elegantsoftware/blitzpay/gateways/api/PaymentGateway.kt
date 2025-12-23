package de.elegantsoftware.blitzpay.gateways.api

import de.elegantsoftware.blitzpay.gateways.api.GatewayType
import de.elegantsoftware.blitzpay.gateways.support.GatewayFactory
import org.springframework.stereotype.Service


@Service
class PaymentGateway(
    private val gatewayFactory: GatewayFactory
) {

    fun getProvider(type: GatewayType): GatewayProvider {
        return gatewayFactory.getProvider(type)
    }

    fun getDefaultProvider(): GatewayProvider {
        return gatewayFactory.getDefaultProvider()
    }

    fun getAvailableProviders(): List<GatewayType> {
        return gatewayFactory.getAvailableProviders()
    }
}