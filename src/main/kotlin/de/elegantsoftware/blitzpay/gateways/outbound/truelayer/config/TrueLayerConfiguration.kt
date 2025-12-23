package de.elegantsoftware.blitzpay.gateways.outbound.truelayer.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.web.client.RestClient

@Configuration
@EnableConfigurationProperties(TrueLayerProperties::class)
class TrueLayerConfiguration {
    
    @Bean
    @Primary
    fun trueLayerRestClient(properties: TrueLayerProperties): RestClient {
        val builder = RestClient.builder()
            .baseUrl(properties.apiBase)
        
        // Add logging interceptor if enabled
        if (properties.httpLogs) {
            builder.requestInterceptor { request, body, execution ->
                val logger = org.slf4j.LoggerFactory.getLogger("TrueLayerHttpClient")
                logger.debug("TrueLayer Request: {} {}", request.method, request.uri)
                request.headers.forEach { name, values ->
                    values.forEach { value ->
                        if (!name.contains("authorization", ignoreCase = true) && 
                            !name.contains("secret", ignoreCase = true)) {
                            logger.debug("Header: $name: $value")
                        }
                    }
                }
                execution.execute(request, body)
            }
        }
        
        return builder.build()
    }
    
    @Bean
    fun trueLayerAuthRestClient(properties: TrueLayerProperties): RestClient {
        return RestClient.builder()
            .baseUrl(properties.authBaseUrl)
            .build()
    }
    
    @Bean
    fun trueLayerObjectMapper(): ObjectMapper {
        return ObjectMapper().findAndRegisterModules()
    }
}