package de.elegantsoftware.blitzpay.config

import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun apiGroup(): GroupedOpenApi =
        GroupedOpenApi.builder()
            .group("quickpay-api")
            .packagesToScan(
                "de.elegantsoftware.blitzpay.merchant",
                "de.elegantsoftware.blitzpay.product",
                "de.elegantsoftware.blitzpay.invoice",
                "de.elegantsoftware.blitzpay.truelayer.inbound"
            )
            .pathsToMatch("/api/**")
            .build()


    @Bean
    fun actuatorGroup(): GroupedOpenApi =
        GroupedOpenApi.builder()
            .group("actuator")
            .pathsToMatch("/actuator/**")
            .build()
}
