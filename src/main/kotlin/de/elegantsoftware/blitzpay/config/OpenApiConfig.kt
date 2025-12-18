package de.elegantsoftware.blitzpay.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("BlitzPay API")
                    .version("1.0.0")
                    .description("""
                        <strong>EU QR Code Payment Platform</strong>
                        
                        <p>A modern payment platform for European merchants with QR code payments.</p>
                        
                        <h4>Key Features:</h4>
                        <ul>
                            <li>Merchant registration and management</li>
                            <li>Product catalog management</li>
                            <li>Invoice generation and tracking</li>
                            <li>QR code payment integration</li>
                            <li>Real-time payment notifications</li>
                            <li>Multi-currency support (EUR, GBP, etc.)</li>
                        </ul>
                        
                        <h4>Authentication:</h4>
                        <ul>
                            <li>JWT Bearer token required for protected endpoints</li>
                            <li>Get your token from the <code>/api/v1/auth/login</code> endpoint</li>
                        </ul>
                    """.trimIndent())
                    .contact(
                        Contact()
                            .name("BlitzPay Support")
                            .email("support@elegantsoftware.de")
                            .url("https://blitzpay.elegantsoftware.de")
                    )
                    .license(
                        License()
                            .name("Commercial License")
                            .url("https://blitzpay.elegantsoftware.de/license")
                    )
                    .termsOfService("https://blitzpay.elegantsoftware.de/terms")
            )
            .servers(
                listOf(
                    Server()
                        .url("http://localhost:8080")
                        .description("Local Development Server"),
                    Server()
                        .url("https://api.blitzpay.elegantsoftware.de")
                        .description("Production Server")
                )
            )
            .components(
                io.swagger.v3.oas.models.Components()
                    .addSecuritySchemes(
                        "bearerAuth",
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .`in`(SecurityScheme.In.HEADER)
                            .description("JWT Bearer Token")
                    )
            )
            .addSecurityItem(
                io.swagger.v3.oas.models.security.SecurityRequirement()
                    .addList("bearerAuth")
            )
    }
}