package com.elegant.software.quickpay.config

import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun paymentsGroup(): GroupedOpenApi =
        GroupedOpenApi.builder()
            .group("payments")
            // adjust to your controllers’ paths
            .pathsToMatch("" +
                    "/payments/**")
            .build()

    @Bean
    fun actuatorGroup(): GroupedOpenApi =
        GroupedOpenApi.builder()
            .group("actuator")
            .pathsToMatch("/actuator/**")
            .build()
}