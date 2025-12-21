package de.elegantsoftware.blitzpay.merchant.support.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "blitzpay.merchant")
data class MerchantProperties(
    @DefaultValue("true")
    val emailVerificationRequired: Boolean = true,

    @DefaultValue("24")
    val verificationTokenValidityHours: Int = 24,

    @DefaultValue("3")
    val maxVerificationAttempts: Int = 3,

    @DefaultValue("http://localhost:8080")
    val verificationBaseUrl: String = "http://localhost:8080",

    @DefaultValue("5")
    val maxResendAttempts: Int = 5,

    @DefaultValue("15")
    val resendCooldownMinutes: Int = 15
)