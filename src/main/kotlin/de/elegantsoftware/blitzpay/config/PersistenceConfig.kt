package de.elegantsoftware.blitzpay.config

import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EntityScan(basePackages = [
    "de.elegantsoftware.blitzpay.merchant.domain",
    "de.elegantsoftware.blitzpay.merchant.outbound.persistence",
    "de.elegantsoftware.blitzpay.invoice.outbound.persistence"
])
@EnableJpaRepositories(basePackages = [
    "de.elegantsoftware.blitzpay.merchant.outbound.persistence",
    "de.elegantsoftware.blitzpay.invoice.outbound.persistence"
])
class PersistenceConfig