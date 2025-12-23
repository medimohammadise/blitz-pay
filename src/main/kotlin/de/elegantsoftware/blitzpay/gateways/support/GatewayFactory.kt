package de.elegantsoftware.blitzpay.gateways.support

import de.elegantsoftware.blitzpay.gateways.api.GatewayProvider
import de.elegantsoftware.blitzpay.gateways.api.GatewayType
import org.springframework.stereotype.Component

@Component
class GatewayFactory(
    private val providers: List<GatewayProvider>
) {
    
    private val providerMap = providers.associateBy { it.getType() }
    
    fun getProvider(type: GatewayType): GatewayProvider {
        return providerMap[type] ?: throw IllegalArgumentException("No provider found for type: $type")
    }
    
    fun getDefaultProvider(): GatewayProvider {
        return getProvider(GatewayType.TRUELAYER)
    }
    
    fun getAvailableProviders(): List<GatewayType> {
        return providerMap.keys.toList()
    }
}