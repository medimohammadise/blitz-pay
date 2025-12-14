package de.elegantsoftware.blitzpay

import de.elegantsoftware.blitzpay.truelayer.support.QrCodeProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableConfigurationProperties(QrCodeProperties::class)
@EnableScheduling
class BlitzpayApplication

fun main(args: Array<String>) {
    runApplication<BlitzpayApplication>(*args)
}
