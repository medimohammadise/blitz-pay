package de.elegantsoftware.blitzpay

import de.elegantsoftware.blitzpay.truelayer.support.QrCodeProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableConfigurationProperties(QrCodeProperties::class)
@EnableScheduling
@EntityScan(basePackages = [
    "de.elegantsoftware.blitzpay.common.domain",
    "de.elegantsoftware.blitzpay.merchant.domain",
    "de.elegantsoftware.blitzpay.sales.domain"
])
@EnableJpaRepositories(basePackages = [
    "de.elegantsoftware.blitzpay.merchant.domain",
    "de.elegantsoftware.blitzpay.sales.domain"
])

class BlitzpayApplication

fun main(args: Array<String>) {
    runApplication<BlitzpayApplication>(*args)
}
