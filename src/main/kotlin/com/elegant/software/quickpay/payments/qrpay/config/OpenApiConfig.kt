package com.elegant.software.quickpay.payments.qrpay.config


import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class QrpayOpenApiConfig {
    @Bean
    fun qrpayApi(): GroupedOpenApi =
        GroupedOpenApi.builder()
            .group("qrpay")
            .packagesToScan("com.elegant.software.quickpay.payments.qrpay")
            .pathsToMatch("/payments/**", "/qr-payments/**")
            .build()
}
